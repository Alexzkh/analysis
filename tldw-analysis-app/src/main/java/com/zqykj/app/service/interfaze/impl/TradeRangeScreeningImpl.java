/**
 * @作者 Mcj
 */
package com.zqykj.app.service.interfaze.impl;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.fasterxml.jackson.core.type.TypeReference;
import com.zqykj.app.service.config.ThreadPoolConfig;
import com.zqykj.app.service.factory.param.agg.TradeRangeScreeningAggParamFactory;
import com.zqykj.app.service.factory.param.query.TradeRangeScreeningQueryParamFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.interfaze.ITradeRangeScreening;
import com.zqykj.app.service.vo.fund.*;
import com.zqykj.common.util.EasyExcelUtils;
import com.zqykj.common.vo.DateRangeRequest;
import com.zqykj.common.vo.SortRequest;
import com.zqykj.domain.Page;
import com.zqykj.domain.PageRequest;
import com.zqykj.domain.Sort;
import com.zqykj.domain.bank.BankTransactionRecord;
import com.zqykj.domain.bank.TradeRangeOperationRecord;
import com.zqykj.infrastructure.core.ServerResponse;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.query.DateRange;
import com.zqykj.parameters.query.QueryOperator;
import com.zqykj.parameters.query.QuerySpecialParams;
import com.zqykj.util.BigDecimalUtil;
import com.zqykj.util.JacksonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * <h1> 交易区间筛选 </h1>
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TradeRangeScreeningImpl extends FundTacticsCommonImpl implements ITradeRangeScreening {

    private final TradeRangeScreeningQueryParamFactory tradeRangeScreeningQueryParamFactory;
    private final TradeRangeScreeningAggParamFactory tradeRangeScreeningAggParamFactory;

    @Override
    public ServerResponse<TradeRangeScreeningDataChartResult> getDataChartResult(TradeRangeScreeningDataChartRequest request) {

        // 分为全部查询与选择个体查询(实际就是一组固定数量的调单卡号集合作为条件)
        if (request.getAnalysisType() == 2 || request.getAnalysisType() == 3) {
            // 个体 / 一组调单卡号集合查询
            return ServerResponse.createBySuccess(selectIndividualQuery(request));
        } else {
            // 全部查询
            return ServerResponse.createBySuccess(selectAllQuery(request));
        }
    }

    /**
     * <h2> 选择个体/一组调单卡号集合作为条件 </h2>
     */
    private TradeRangeScreeningDataChartResult selectIndividualQuery(TradeRangeScreeningDataChartRequest request) {

        int from = request.getStartNumberOfTrade() - 1;
        int size = (request.getEndNumberOfTrade() - request.getStartNumberOfTrade()) + 1;

        // 查询固定交易笔数的 交易金额集合
        List<BigDecimal> tradeAmounts = queryTradeAmounts(request, from, size);
        // 查询固定交易笔数的 入账金额集合
        List<BigDecimal> creditAmounts = queryCreditAmounts(request, from, size);
        // 查询固定交易笔数的 出账金额集合
        List<BigDecimal> payoutAmounts = queryPayoutAmounts(request, from, size);
        return new TradeRangeScreeningDataChartResult(payoutAmounts, creditAmounts, tradeAmounts);
    }

    /**
     * <h2> 全部查询 </h2>
     */
    private TradeRangeScreeningDataChartResult selectAllQuery(TradeRangeScreeningDataChartRequest request) {

        // 检查调单卡号的数量
        DateRangeRequest dateRange = request.getDateRange();
        String start = dateRange.getStart() + dateRange.getTimeEnd();
        String end = dateRange.getEnd() + dateRange.getTimeStart();
        // TODO 超过此最大调单卡号限制的阈值的话,即便查询出来,还需要作为参数传递给es(不可能将所有的调单的卡号查询出来作为参数),查询很慢
        // TODO 可以参考交易汇聚和交易统计使用的查询全部方法
//        if (checkAdjustCardCountByDate(request.getCaseId(), new DateRange(start, end))) {
        // 查询固定最大调单卡号
        List<String> maxAdjustCards = queryMaxAdjustCardsByDate(request.getCaseId(), new DateRange(start, end));
        if (!CollectionUtils.isEmpty(maxAdjustCards)) {
            request.setCardNums(maxAdjustCards);
            return selectIndividualQuery(request);
        }
//        } else {
//            // TODO 超过最大调单数量暂不处理(数据量太大)
//
//        }
        return new TradeRangeScreeningDataChartResult();
    }

    public ServerResponse<String> saveOperationRecord(TradeRangeScreeningSaveRequest request) {

        TradeRangeOperationRecord operationRecord = convertTradeRangeOperationRecordFromSaveRequest(request);
        try {
            entranceRepository.save(operationRecord, request.getCaseId(), TradeRangeOperationRecord.class);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("保存记录失败,系统内部错误!");
            return ServerResponse.createByErrorMessage("保存记录失败!");
        }
        return ServerResponse.createBySuccess("成功");
    }

    public ServerResponse<String> deleteOperationRecord(String caseId, String id) {

        try {
            entranceRepository.deleteById(id, caseId, TradeRangeOperationRecord.class);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("删除记录失败,系统内部错误!");
            return ServerResponse.createByErrorMessage("删除记录失败!");
        }
        return ServerResponse.createBySuccess("成功");
    }

    public ServerResponse<FundAnalysisResultResponse<TradeRangeOperationRecord>> operationRecordsList(FundTacticsPartGeneralRequest request) {

        QuerySpecialParams query = queryRequestParamFactory.queryDataByCaseId(request.getCaseId());
        query.setIncludeFields(FundTacticsAnalysisField.tradeRangeScreeningQueryFields());
        com.zqykj.common.vo.PageRequest pageRequest = request.getPageRequest();
        // 默认按保存日期降序排序
        SortRequest sortRequest = request.getSortRequest();
        String[] orderProperty = new String[]{sortRequest.getProperty()};
        // 如果排序的就是 金额范围的话
        if (sortRequest.getProperty().equals("fund_range")) {
            orderProperty = new String[]{"min_amount", "max_amount"};
        }
        Page<TradeRangeOperationRecord> page = entranceRepository.findAll(PageRequest.of(pageRequest.getPage(), pageRequest.getPageSize(),
                Sort.Direction.valueOf(sortRequest.getOrder().name()), orderProperty), request.getCaseId(), TradeRangeOperationRecord.class, query);
        List<TradeRangeOperationRecord> content = page.getContent();
        for (TradeRangeOperationRecord operationRecord : content) {
            operationRecord.setOperationDateFormat(DateFormatUtils.format(operationRecord.getOperationDate(), "yyyy-MM-dd HH:mm:ss"));
        }
        return ServerResponse.createBySuccess(FundAnalysisResultResponse.build(content, page.getTotalElements(), pageRequest.getPageSize()));
    }

    public ServerResponse<FundAnalysisResultResponse<TradeRangeOperationDetailSeeResult>> seeOperationRecordsDetailList(FundTacticsPartGeneralRequest request, int from, int size) {

        SortRequest sortRequest = request.getSortRequest();
        // 根据操作记录id 查询对应的操作记录
        TradeRangeOperationRecord tradeRangeOperationRecord = getOperationRecordsAdjustCardsById(request.getCaseId(), request.getId());
        if (null == tradeRangeOperationRecord) {
            return ServerResponse.createByErrorMessage("您查询的操作记录已被删除,请核实!");
        }
        Double minAmount = tradeRangeOperationRecord.getMinAmount();
        Double maxAmount = tradeRangeOperationRecord.getMaxAmount();
        int dateType = tradeRangeOperationRecord.getDataCategory();
        List<String> adjustCards = null;
        if (CollectionUtils.isEmpty(request.getExportIds())) {
            if (tradeRangeOperationRecord.getIndividualBankCardsNumber() == -1) {
                // 查询最大调单卡号
                // TODO 不可能全部查询出来作为参数,当然你可以去 表 BankTransactionRecord 查询,然后查询的记录, 去看它的查询卡号 在 表 BankTransactionFlow 的查询卡号中是否存在
                adjustCards = queryMaxAdjustCards(request.getCaseId(), minAmount, QueryOperator.gte, maxAmount, QueryOperator.lte, null);
            } else {
                // 查询卡号的进账、出账记录
                adjustCards = tradeRangeOperationRecord.getAdjustCards();
            }
            if (CollectionUtils.isEmpty(adjustCards)) {
                return ServerResponse.createBySuccess(FundAnalysisResultResponse.empty());
            }
        }
        QuerySpecialParams query = tradeRangeScreeningQueryParamFactory.queryAdjustCardsTradeRecord(request.getCaseId(), request.getExportIds(), adjustCards, minAmount, maxAmount, dateType);
        // 设置queryFields
        if (from == 0 && size == 0) {
            query.setIncludeFields(new String[0]);
        } else {
            query.setIncludeFields(FundTacticsAnalysisField.tradeRangeOperationDetailQueryFields());
        }
        Page<BankTransactionRecord> page = entranceRepository.findAll(PageRequest.of(from, size, Sort.Direction.valueOf(sortRequest.getOrder().name()), sortRequest.getProperty()),
                request.getCaseId(), BankTransactionRecord.class, query);
        if (null == page) {
            return ServerResponse.createBySuccess(FundAnalysisResultResponse.empty());
        }
        List<BankTransactionRecord> content = page.getContent();
        List<TradeRangeOperationDetailSeeResult> newContent = content.stream().map(this::convertFromTradeRangeOperationDetailSeeResult).collect(Collectors.toList());
        return ServerResponse.createBySuccess(FundAnalysisResultResponse.build(newContent, page.getTotalElements(), page.getSize()));
    }

    public ServerResponse<String> operationRecordsDetailListExport(ExcelWriter excelWriter, FundTacticsPartGeneralRequest request) throws Exception {

        // 获取总量
        ServerResponse<FundAnalysisResultResponse<TradeRangeOperationDetailSeeResult>> response = seeOperationRecordsDetailList(request, 0, 1);
        if (!response.isSuccess()) {
            return ServerResponse.createByErrorMessage("导出失败,系统内部错误!");
        }
        int total = Integer.parseInt(String.valueOf(response.getData().getTotal()));
        if (StringUtils.isBlank(request.getExportFileName())) {
            request.setExportFileName("交易区间筛选交易流水");
        }
        export(excelWriter, total, request);
        return ServerResponse.createBySuccess();
    }


    private void export(ExcelWriter excelWriter, int total, FundTacticsPartGeneralRequest request) throws Exception {

        if (total == 0) {
            excelWriter.write(new ArrayList<>(), EasyExcelUtils.generateWriteSheet(request.getExportFileName()));
        }
        // 判断处理sheet 页的个数
        if (total < exportThresholdConfig.getPerSheetRowCount()) {
            // 单个sheet页即可
            WriteSheet sheet = EasyExcelUtils.generateWriteSheet(request.getExportFileName());
            addSheetData(0, total, request, excelWriter, sheet);
        } else {
            // 多个sheet页处理
            Integer sheetNo = 0;
            int limit = total;
            if (total > exportThresholdConfig.getExcelExportThreshold()) {
                limit = exportThresholdConfig.getExcelExportThreshold();
            }
            int position = 0;
            int perSheetRowCount = exportThresholdConfig.getPerSheetRowCount();
            // 这里就没必要在多线程了(一个sheet页假设50W,内部分批次查询,每次查询1W,就要查詢50次,若这里再开多线程分批次,ThreadPoolConfig.getExecutor()
            // 的最大线程就这么多,剩下的只能在队列中等待)
            while (position < limit) {
                int next = Math.min(position + perSheetRowCount, limit);
                WriteSheet sheet;
                if (sheetNo == 0) {
                    sheet = EasyExcelUtils.generateWriteSheet(sheetNo, request.getExportFileName());
                } else {
                    sheet = EasyExcelUtils.generateWriteSheet(sheetNo, request.getExportFileName() + "-" + sheetNo);
                }
                addSheetData(position, next, request, excelWriter, sheet);
                position = next;
                sheetNo++;
            }
        }
    }

    private void addSheetData(int position, int limit, FundTacticsPartGeneralRequest request, ExcelWriter writer, WriteSheet sheet) throws ExecutionException, InterruptedException {

        if (limit == 0) {
            return;
        }
        int chunkSize = exportThresholdConfig.getPerWriteRowCount();
        List<Future<List<TradeRangeOperationDetailSeeResult>>> futures = new ArrayList<>();
        if (limit <= exportThresholdConfig.getPerSheetRowCount()) {
            while (position < limit) {
                int next = Math.min(position + chunkSize, limit);
                int finalPosition = position;
                CompletableFuture<List<TradeRangeOperationDetailSeeResult>> future =
                        CompletableFuture.supplyAsync(() -> queryDetail(request, finalPosition, next - finalPosition), ThreadPoolConfig.getExecutor());
                position = next;
                futures.add(future);
            }
            for (Future<List<TradeRangeOperationDetailSeeResult>> future : futures) {
                List<TradeRangeOperationDetailSeeResult> dataList = future.get();
                // 添加sheet
                writer.write(dataList, sheet);
            }
        }
    }

    private List<TradeRangeOperationDetailSeeResult> queryDetail(FundTacticsPartGeneralRequest request, int from, int size) {

        ServerResponse<FundAnalysisResultResponse<TradeRangeOperationDetailSeeResult>> response = seeOperationRecordsDetailList(request, from, size);
        if (!response.isSuccess()) {
            throw new RuntimeException("导出操作记录失败,查询出错!");
        }
        return response.getData().getContent();
    }

    public ServerResponse<FundAnalysisResultResponse<TradeOperationIndividualBankCardsStatistical>> seeIndividualBankCardsStatisticalResult(FundTacticsPartGeneralRequest request) {

        com.zqykj.common.vo.PageRequest pageRequest = request.getPageRequest();
        int offset = com.zqykj.common.vo.PageRequest.getOffset(pageRequest.getPage(), pageRequest.getPageSize());
        SortRequest sortRequest = request.getSortRequest();
        // 查询唯一标识id的数据,取出保存的调单卡号
        QuerySpecialParams queryAdjustCards = queryRequestParamFactory.queryByIdAndCaseId(request.getCaseId(), request.getId());
        String[] queryFields = new String[]{FundTacticsAnalysisField.TradeRangeScreening.ADJUST_CARD, FundTacticsAnalysisField.TradeRangeScreening.MIN_AMOUNT,
                FundTacticsAnalysisField.TradeRangeScreening.MAX_AMOUNT, FundTacticsAnalysisField.TradeRangeScreening.INDIVIDUAL_BANKCARDS_NUMBER, FundTacticsAnalysisField.TradeRangeScreening.DATA_CATEGORY};
        queryAdjustCards.setIncludeFields(queryFields);
        Page<TradeRangeOperationRecord> page = entranceRepository.findAll(PageRequest.of(0, 1), request.getCaseId(), TradeRangeOperationRecord.class, queryAdjustCards);
        if (CollectionUtils.isEmpty(page.getContent())) {
            return ServerResponse.createBySuccess(FundAnalysisResultResponse.empty());
        }
        if (page.getContent().get(0).getIndividualBankCardsNumber() == -1) {
            throw new IllegalArgumentException("全部查询暂不支持分析个体银行卡统计结果");
        }
        // 该条操作记录保存的调单卡号集合
        List<String> adjustCards = page.getContent().get(0).getAdjustCards();
        // 查询个体银行卡统计
        // TODO 如果需求就是查询的全部,就去除操作记录的条件
        Double minAmount = page.getContent().get(0).getMinAmount();
        Double maxAmount = page.getContent().get(0).getMaxAmount();
        int dataType = page.getContent().get(0).getDataCategory();
        QuerySpecialParams query = tradeRangeScreeningQueryParamFactory.queryIndividualBankCardsStatistical(request.getCaseId(), null, adjustCards, minAmount, maxAmount, dataType);
        // 设置queryFields
        query.setIncludeFields(new String[]{FundTacticsAnalysisField.QUERY_CARD, FundTacticsAnalysisField.BANK});
        AggregationParams agg = tradeRangeScreeningAggParamFactory.individualBankCardsStatisticalAgg(offset, pageRequest.getPageSize(), sortRequest.getProperty(),
                sortRequest.getOrder().name(), adjustCards.size());
        agg.setResultName("individualBankCardsStatistical");
        AggregationParams totalAgg = total();
        Map<String, String> aggKeyMapping = new LinkedHashMap<>();
        Map<String, String> entityAggKeyMapping = new LinkedHashMap<>();
        entityMappingFactory.buildTradeAnalysisResultAggMapping(aggKeyMapping, entityAggKeyMapping, TradeOperationIndividualBankCardsStatistical.class);
        agg.setMapping(aggKeyMapping);
        agg.addSiblingAggregation(totalAgg);
        Map<String, List<List<Object>>> results = entranceRepository.compoundQueryAndAgg(query, agg, BankTransactionRecord.class, request.getCaseId());
        if (CollectionUtils.isEmpty(results) || CollectionUtils.isEmpty(results.get(agg.getResultName()))) {
            return ServerResponse.createBySuccess(FundAnalysisResultResponse.empty());
        }
        List<List<Object>> result = results.get(agg.getResultName());
        List<String> entityTitles = new ArrayList<>(entityAggKeyMapping.keySet());
        List<Map<String, Object>> entityPropertyValueMapping = parseFactory.convertEntity(result, entityTitles, TradeConvergenceAnalysisResult.class);
        List<TradeOperationIndividualBankCardsStatistical> statisticalResults = JacksonUtils.parse(entityPropertyValueMapping, new TypeReference<List<TradeOperationIndividualBankCardsStatistical>>() {
        });
        // 保留2位小数,转化科学计算方式的金额
        statisticalResults.forEach(TradeOperationIndividualBankCardsStatistical::amountReservedTwo);
        long total = 0;
        if (!CollectionUtils.isEmpty(results.get(CARDINALITY_TOTAL))) {
            total = (long) results.get(CARDINALITY_TOTAL).get(0).get(0);
        }
        return ServerResponse.createBySuccess(FundAnalysisResultResponse.build(statisticalResults, total, pageRequest.getPageSize()));
    }

    /**
     * <h2> 获取对应操作记录保存的调单卡号集合(通过id 和 caseId查询) </h2>
     */
    private TradeRangeOperationRecord getOperationRecordsAdjustCardsById(String caseId, String id) {

        String[] queryFields = new String[]{FundTacticsAnalysisField.TradeRangeScreening.ADJUST_CARD, FundTacticsAnalysisField.TradeRangeScreening.MIN_AMOUNT,
                FundTacticsAnalysisField.TradeRangeScreening.MAX_AMOUNT, FundTacticsAnalysisField.TradeRangeScreening.INDIVIDUAL_BANKCARDS_NUMBER, FundTacticsAnalysisField.TradeRangeScreening.DATA_CATEGORY};
        QuerySpecialParams query = queryRequestParamFactory.queryByIdAndCaseId(caseId, id);
        query.setIncludeFields(queryFields);
        Page<TradeRangeOperationRecord> page = entranceRepository.findAll(PageRequest.of(0, 1), caseId, TradeRangeOperationRecord.class, query);
        List<TradeRangeOperationRecord> content = page.getContent();
        if (CollectionUtils.isEmpty(content)) {
            return null;
        }
        return content.get(0);
    }


    /**
     * <h2> 查询固定交易笔数的交易金额 </h2>
     */
    private List<BigDecimal> queryTradeAmounts(TradeRangeScreeningDataChartRequest request, int from, int size) {

        QuerySpecialParams queryTradeAmount = tradeRangeScreeningQueryParamFactory.queryTradeAmount(request);
        String property = FundTacticsAnalysisField.CHANGE_MONEY;
        Page<BankTransactionRecord> page = entranceRepository.findAll(PageRequest.of(from, size, Sort.Direction.ASC, property),
                request.getCaseId(), BankTransactionRecord.class, queryTradeAmount);
        return mapAmount(page.getContent());
    }

    /**
     * <h2> 查询固定交易笔数的进账金额 </h2>
     */
    private List<BigDecimal> queryCreditAmounts(TradeRangeScreeningDataChartRequest request, int from, int size) {

        QuerySpecialParams queryTradeAmount = tradeRangeScreeningQueryParamFactory.queryCreditOrPayoutAmount(request, true);
        String property = FundTacticsAnalysisField.CHANGE_MONEY;
        Page<BankTransactionRecord> page = entranceRepository.findAll(PageRequest.of(from, size, Sort.Direction.ASC, property),
                request.getCaseId(), BankTransactionRecord.class, queryTradeAmount);
        return mapAmount(page.getContent());
    }

    /**
     * <h2> 查询固定交易笔数的出账金额 </h2>
     */
    private List<BigDecimal> queryPayoutAmounts(TradeRangeScreeningDataChartRequest request, int from, int size) {

        QuerySpecialParams queryTradeAmount = tradeRangeScreeningQueryParamFactory.queryCreditOrPayoutAmount(request, false);
        String property = FundTacticsAnalysisField.CHANGE_MONEY;
        Page<BankTransactionRecord> page = entranceRepository.findAll(PageRequest.of(from, size, Sort.Direction.ASC, property),
                request.getCaseId(), BankTransactionRecord.class, queryTradeAmount);
        return mapAmount(page.getContent());
    }

    private List<BigDecimal> mapAmount(List<BankTransactionRecord> results) {
        return results.stream().map(e -> BigDecimalUtil.value(e.getChangeAmount())).collect(Collectors.toList());
    }

    /**
     * <h2> 生成交易区间筛选操作记录 </h2>
     */
    private TradeRangeOperationRecord convertTradeRangeOperationRecordFromSaveRequest(TradeRangeScreeningSaveRequest request) {

        TradeRangeOperationRecord operationRecord = new TradeRangeOperationRecord();
        operationRecord.setCaseId(request.getCaseId());
        operationRecord.setAccountOpeningIDNumber(request.getAccountOpeningNumber());
        operationRecord.setAccountOpeningName(request.getAccountOpeningName());
        if (!CollectionUtils.isEmpty(request.getCardNum())) {
            operationRecord.setAdjustCards(request.getCardNum());
            operationRecord.setIndividualBankCardsNumber(request.getCardNum().size());
        } else {
            // 代表全部查询
            operationRecord.setIndividualBankCardsNumber(-1);
        }
        operationRecord.setDataCategory(request.getSaveType());
        // 操作人是当前登录用户
        operationRecord.setOperationPeople(request.getOperationPeople());
        operationRecord.setMinAmount(request.getFundMin());
        operationRecord.setMaxAmount(request.getFundMax());
        operationRecord.setRemark(request.getRemark());
        operationRecord.setOperationDate(new Date());
        return operationRecord;
    }

    private TradeRangeOperationDetailSeeResult convertFromTradeRangeOperationDetailSeeResult(BankTransactionRecord record) {

        TradeRangeOperationDetailSeeResult result = new TradeRangeOperationDetailSeeResult();
        BeanUtils.copyProperties(record, result);
        // 重新设置日期时间
        result.setTradingTime(DateFormatUtils.format(record.getTradingTime(), "yyyy-MM-dd HH:mm:ss"));
        // 重新设置交易金额
        result.setTradingAmount(BigDecimalUtil.value(record.getChangeAmount()));
        return result;
    }

    private AggregationParams total() {
        AggregationParams agg = aggParamFactory.buildDistinctViaField(FundTacticsAnalysisField.QUERY_CARD);
        agg.setMapping(entityMappingFactory.buildDistinctTotalAggMapping(FundTacticsAnalysisField.QUERY_CARD));
        agg.setResultName(CARDINALITY_TOTAL);
        return agg;
    }
}
