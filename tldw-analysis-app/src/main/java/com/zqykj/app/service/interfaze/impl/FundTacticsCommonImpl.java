/**
 * @作者 Mcj
 */
package com.zqykj.app.service.interfaze.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.zqykj.app.service.config.FundTacticsThresholdConfigProperties;
import com.zqykj.app.service.config.ThreadPoolConfig;
import com.zqykj.app.service.factory.AggregationEntityMappingFactory;
import com.zqykj.app.service.factory.AggregationRequestParamFactory;
import com.zqykj.app.service.factory.AggregationResultEntityParseFactory;
import com.zqykj.app.service.factory.QueryRequestParamFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.field.FundTacticsFuzzyQueryField;
import com.zqykj.app.service.vo.fund.FundAnalysisResultResponse;
import com.zqykj.app.service.vo.fund.FundTacticsPartGeneralRequest;
import com.zqykj.app.service.vo.fund.middle.TradeAnalysisDetailResult;
import com.zqykj.common.core.ServerResponse;
import com.zqykj.common.util.EasyExcelUtils;
import com.zqykj.common.vo.SortRequest;
import com.zqykj.domain.Page;
import com.zqykj.domain.PageRequest;
import com.zqykj.domain.Sort;
import com.zqykj.domain.bank.BankTransactionFlow;
import com.zqykj.domain.bank.BankTransactionRecord;
import com.zqykj.parameters.Pagination;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.query.DateRange;
import com.zqykj.parameters.query.QueryOperator;
import com.zqykj.parameters.query.QuerySpecialParams;
import com.zqykj.repository.EntranceRepository;
import com.zqykj.util.BigDecimalUtil;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateParser;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public abstract class FundTacticsCommonImpl {

    @Autowired
    @SuppressWarnings("all")
    protected EntranceRepository entranceRepository;
    @Autowired
    protected AggregationEntityMappingFactory entityMappingFactory;
    @Autowired
    protected AggregationRequestParamFactory aggParamFactory;
    @Autowired
    protected QueryRequestParamFactory queryRequestParamFactory;
    @Autowired
    protected AggregationResultEntityParseFactory parseFactory;
    @Autowired
    protected FundTacticsThresholdConfigProperties fundThresholdConfig;
    @Autowired
    protected FundTacticsThresholdConfigProperties.Export exportThresholdConfig;

    protected static final String CARDINALITY_TOTAL = "cardinality_total";
    protected static final DateParser DATE_PARSER = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");

    /**
     * <h2> 检查调单卡号数量 </h2>
     */
    public boolean checkAdjustCardCount(String caseId, Double startAmount, QueryOperator startOperator,
                                        Double endAmount, QueryOperator endOperator, DateRange dateRange) {

        QuerySpecialParams querySpecialParams = queryRequestParamFactory.queryAdjustNumberByAmountAndDate(caseId, startAmount, startOperator, endAmount, endOperator, dateRange);
        return getDistinctAgg(caseId, querySpecialParams);
    }

    public boolean checkAdjustCardCountBySingleAmountDate(String caseId, Double startAmount, QueryOperator startOperator, DateRange dateRange) {

        QuerySpecialParams querySpecialParams = queryRequestParamFactory.queryAdjustNumberByAmountAndDate(caseId, startAmount, startOperator, dateRange);
        return getDistinctAgg(caseId, querySpecialParams);
    }

    /**
     * <h2> 检查调单卡号数量 </h2>
     */
    public boolean checkAdjustCardCountByDate(String caseId, DateRange dateRange) {

        QuerySpecialParams querySpecialParams = queryRequestParamFactory.queryAdjustNumberByDate(caseId, dateRange);
        return getDistinctAgg(caseId, querySpecialParams);
    }

    /**
     * <h2> 获取满足条件的调单卡号 </h2>
     * <p>
     * 最大数量为 maxAdjustCardQueryCount
     */
    public List<String> queryMaxAdjustCards(String caseId, Double startAmount, QueryOperator startOperator,
                                            Double endAmount, QueryOperator endOperator, DateRange dateRange) {

        QuerySpecialParams querySpecialParams = queryRequestParamFactory.queryAdjustNumberByAmountAndDate(caseId, startAmount, startOperator, endAmount, endOperator, dateRange);
        return getGroupAgg(caseId, querySpecialParams);
    }

    public List<String> queryMaxAdjustCardsBySingleAmountDate(String caseId, Double startAmount, QueryOperator startOperator, DateRange dateRange) {

        QuerySpecialParams querySpecialParams = queryRequestParamFactory.queryAdjustNumberByAmountAndDate(caseId, startAmount, startOperator, dateRange);
        return getGroupAgg(caseId, querySpecialParams);
    }

    /**
     * <h2> 查询最大调单卡号 </h2>
     */
    public List<String> queryMaxAdjustCardsByDate(String caseId, DateRange dateRange) {

        QuerySpecialParams querySpecialParams = queryRequestParamFactory.queryAdjustNumberByDate(caseId, dateRange);
        return getGroupAgg(caseId, querySpecialParams);
    }

    private List<String> getGroupAgg(String caseId, QuerySpecialParams querySpecialParams) {
        int maxAdjustCardCount = fundThresholdConfig.getMaxAdjustCardCount();
        AggregationParams aggregationParams = aggParamFactory.groupByAndCountField(FundTacticsAnalysisField.QUERY_CARD, maxAdjustCardCount, new Pagination(0, maxAdjustCardCount));
        aggregationParams.setMapping(entityMappingFactory.buildGroupByAggMapping(FundTacticsAnalysisField.QUERY_CARD));
        aggregationParams.setResultName("adjustCards");
        Map<String, List<List<Object>>> compoundQueryAndAgg = entranceRepository.compoundQueryAndAgg(querySpecialParams, aggregationParams, BankTransactionFlow.class, caseId);
        List<List<Object>> cards = compoundQueryAndAgg.get(aggregationParams.getResultName());
        if (CollectionUtils.isEmpty(cards)) {
            return null;
        }
        return cards.stream().map(e -> e.get(0).toString()).collect(Collectors.toList());
    }

    private boolean getDistinctAgg(String caseId, QuerySpecialParams querySpecialParams) {
        AggregationParams aggregationParams = aggParamFactory.buildDistinctViaField(FundTacticsAnalysisField.QUERY_CARD);
        aggregationParams.setMapping(entityMappingFactory.buildDistinctTotalAggMapping(FundTacticsAnalysisField.QUERY_CARD));
        aggregationParams.setResultName("adjustCardTotal");
        Map<String, List<List<Object>>> compoundQueryAndAgg = entranceRepository.compoundQueryAndAgg(querySpecialParams, aggregationParams, BankTransactionFlow.class, caseId);
        List<List<Object>> total = compoundQueryAndAgg.get(aggregationParams.getResultName());
        if (CollectionUtils.isEmpty(total)) {
            return true;
        }
        long count = Long.parseLong(total.get(0).get(0).toString());
        return count <= fundThresholdConfig.getMaxAdjustCardCount();
    }

    protected ServerResponse<FundAnalysisResultResponse<TradeAnalysisDetailResult>> detail(FundTacticsPartGeneralRequest request, int from, int size, String... fuzzyFields) {
        com.zqykj.common.vo.PageRequest pageRequest = request.getPageRequest();
        SortRequest sortRequest = request.getSortRequest();
        QuerySpecialParams query = queryRequestParamFactory.queryTradeAnalysisDetail(request.getCaseId(), request.getQueryCard(), request.getOppositeCard(), request.getKeyword(), fuzzyFields);
        // 设置需要查询的字段
        if (from == 0 && size == 0) {
            query.setIncludeFields(new String[0]);
        } else {
            query.setIncludeFields(FundTacticsAnalysisField.detailShowField());
        }
        Page<BankTransactionRecord> page = entranceRepository.findAll(PageRequest.of(from, size, Sort.Direction.valueOf(sortRequest.getOrder().name()), sortRequest.getProperty()),
                request.getCaseId(), BankTransactionRecord.class, query);
        if (page == null || CollectionUtils.isEmpty(page.getContent())) {
            return ServerResponse.createBySuccess(FundAnalysisResultResponse.empty());
        }
        List<TradeAnalysisDetailResult> detailResults = getTradeAnalysisDetailResults(page);
        return ServerResponse.createBySuccess(FundAnalysisResultResponse.build(detailResults, page.getTotalElements(), pageRequest.getPageSize()));
    }

    protected long detailTotal(FundTacticsPartGeneralRequest request) {
        QuerySpecialParams query = queryRequestParamFactory.queryTradeAnalysisDetail(request.getCaseId(), request.getQueryCard(), request.getOppositeCard(), request.getKeyword(), FundTacticsFuzzyQueryField.detailFuzzyFields);
        return entranceRepository.count(request.getCaseId(), BankTransactionRecord.class, query);
    }

    protected List<TradeAnalysisDetailResult> getTradeAnalysisDetailResults(Page<BankTransactionRecord> page) {
        List<TradeAnalysisDetailResult> detailResults = new ArrayList<>();
        List<BankTransactionRecord> content = page.getContent();
        content.forEach(e -> {
            TradeAnalysisDetailResult detailResult = new TradeAnalysisDetailResult();
            BeanUtil.copyProperties(e, detailResult, FundTacticsAnalysisField.CHANGE_MONEY, FundTacticsAnalysisField.TRADING_TIME);
            detailResult.setChangeAmount(BigDecimalUtil.value(e.getChangeAmount()));
            detailResult.setTradeTime(DateFormatUtils.format(e.getTradingTime(), "yyyy-MM-dd HH:mm:ss"));
            detailResults.add(detailResult);
        });
        return detailResults;
    }

    protected List<TradeAnalysisDetailResult> getTradeAnalysisDetailResultsByFlow(Page<BankTransactionFlow> page) {
        List<TradeAnalysisDetailResult> detailResults = new ArrayList<>();
        List<BankTransactionFlow> content = page.getContent();
        content.forEach(e -> {
            TradeAnalysisDetailResult detailResult = new TradeAnalysisDetailResult();
            BeanUtil.copyProperties(e, detailResult, FundTacticsAnalysisField.TRADING_TIME);
            detailResult.setChangeAmount(BigDecimalUtil.value(e.getTransactionMoney()));
            detailResult.setTradeTime(DateFormatUtils.format(e.getTradingTime(), "yyyy-MM-dd HH:mm:ss"));
            detailResults.add(detailResult);
        });
        return detailResults;
    }

    /**
     * <h2> 检查调单卡号数量是否超过了 设置的最大值 maxAdjustCardQueryCount </h2>
     */
    protected boolean checkMaxAdjustCards(FundTacticsPartGeneralRequest request) {
        return checkAdjustCardCountByDate(request.getCaseId(), FundTacticsPartGeneralRequest.getDateRange(request.getDateRange()));
    }

    /**
     * <h2> 获取最大数量的调单卡号 </h2>
     */
    protected List<String> getMaxAdjustCards(FundTacticsPartGeneralRequest request) {
        return queryMaxAdjustCardsByDate(request.getCaseId(), FundTacticsPartGeneralRequest.getDateRange(request.getDateRange()));
    }


    protected void writeSheet(ExcelWriter excelWriter, int total, FundTacticsPartGeneralRequest request) throws Exception {

        if (total == 0) {
            excelWriter.write(new ArrayList<>(), EasyExcelUtils.generateWriteSheet(request.getExportFileName()));
        }
        // 判断处理sheet 页的个数
        if (total < exportThresholdConfig.getPerSheetRowCount()) {
            // 单个sheet页即可
            WriteSheet sheet = EasyExcelUtils.generateWriteSheet(request.getExportFileName());
            writeSheetData(0, total, request, excelWriter, sheet);
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
                writeSheetData(position, next, request, excelWriter, sheet);
                position = next;
                sheetNo++;
            }
        }
    }

    private void writeSheetData(int position, int limit, FundTacticsPartGeneralRequest request, ExcelWriter writer, WriteSheet sheet) throws ExecutionException, InterruptedException {

        if (limit == 0) {
            return;
        }
        int chunkSize = exportThresholdConfig.getPerWriteRowCount();
        List<Future<List<TradeAnalysisDetailResult>>> futures = new ArrayList<>();
        String[] detailFuzzyFields = FundTacticsFuzzyQueryField.detailFuzzyFields;
        if (limit <= exportThresholdConfig.getPerSheetRowCount()) {
            while (position < limit) {
                int next = Math.min(position + chunkSize, limit);
                int finalPosition = position;
                CompletableFuture<List<TradeAnalysisDetailResult>> future =
                        CompletableFuture.supplyAsync(() -> detail(request, finalPosition, next - finalPosition, detailFuzzyFields).getData().getContent(),
                                ThreadPoolConfig.getExecutor());
                position = next;
                futures.add(future);
            }
            for (Future<List<TradeAnalysisDetailResult>> future : futures) {
                List<TradeAnalysisDetailResult> dataList = future.get();
                // 添加sheet
                writer.write(dataList, sheet);
            }
        }
    }
}
