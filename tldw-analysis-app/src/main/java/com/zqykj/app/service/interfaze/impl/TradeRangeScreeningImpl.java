/**
 * @作者 Mcj
 */
package com.zqykj.app.service.interfaze.impl;

import com.zqykj.app.service.factory.param.query.TradeRangeScreeningQueryParamFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.interfaze.ITradeRangeScreening;
import com.zqykj.app.service.vo.fund.*;
import com.zqykj.common.core.ServerResponse;
import com.zqykj.common.vo.DateRangeRequest;
import com.zqykj.common.vo.SortRequest;
import com.zqykj.domain.Page;
import com.zqykj.domain.PageRequest;
import com.zqykj.domain.Sort;
import com.zqykj.domain.bank.BankTransactionRecord;
import com.zqykj.domain.bank.TradeRangeOperationRecord;
import com.zqykj.parameters.query.DateRange;
import com.zqykj.parameters.query.QueryOperator;
import com.zqykj.parameters.query.QuerySpecialParams;
import com.zqykj.util.BigDecimalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <h1> 交易区间筛选 </h1>
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TradeRangeScreeningImpl extends FundTacticsCommonImpl implements ITradeRangeScreening {

    private final TradeRangeScreeningQueryParamFactory queryParamFactory;

    @Override
    public ServerResponse<TradeRangeScreeningDataChartResult> getDataChartResult(TradeRangeScreeningDataChartRequest request) {

        // 分为全部查询与选择个体查询(实际就是一组固定数量的调单卡号集合作为条件)
        if (CollectionUtils.isEmpty(request.getCardNums())) {
            // 个体 / 一组调单卡号集合查询
            return ServerResponse.createBySuccess(selectIndividualQuery(request));
        } else {
            // 全部查询
            return ServerResponse.createBySuccess(selectAllQuery(request));
        }
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

    public ServerResponse<List<TradeRangeOperationRecord>> operationRecordsList(FundTacticsPartGeneralRequest request) {

        QuerySpecialParams query = queryRequestParamFactory.queryDataByCaseId(request.getCaseId());
        query.setIncludeFields(FundTacticsAnalysisField.tradeRangeScreeningQueryFields());
        com.zqykj.common.vo.PageRequest pageRequest = request.getPageRequest();
        // 默认按保存日期降序排序
        SortRequest sortRequest = request.getSortRequest();
        Page<TradeRangeOperationRecord> page = entranceRepository.findAll(PageRequest.of(pageRequest.getPage(), pageRequest.getPageSize(),
                Sort.Direction.valueOf(sortRequest.getOrder().name()), sortRequest.getProperty()), request.getCaseId(), TradeRangeOperationRecord.class, query);
        List<TradeRangeOperationRecord> content = page.getContent();
        for (TradeRangeOperationRecord operationRecord : content) {
            operationRecord.setOperationDateFormat(DateFormatUtils.format(operationRecord.getOperationDate(), "yyyy-MM-dd HH:mm:ss"));
        }
        return ServerResponse.createBySuccess(content);
    }

    public ServerResponse<List<TradeRangeOperationDetailSeeResult>> seeOperationRecordsDetailList(TradeRangeOperationDetailSeeRequest request) {

        com.zqykj.common.vo.PageRequest pageRequest = request.getPageRequest();
        SortRequest sortRequest = request.getSortRequest();
        // 根据操作记录id 查询对应的操作记录
        TradeRangeOperationRecord tradeRangeOperationRecord = getOperationRecordsAdjustCardsById(request.getCaseId(), request.getId());
        if (null == tradeRangeOperationRecord) {
            return ServerResponse.createByErrorMessage("您查询的操作记录已被删除,请核实!");
        }
        Double minAmount = tradeRangeOperationRecord.getMinAmount();
        Double maxAmount = tradeRangeOperationRecord.getMaxAmount();
        List<String> adjustCards;
        if (request.isQueryAllFlag()) {

            // 检查调单卡号的数量
            if (checkAdjustCardCount(request.getCaseId(), minAmount, QueryOperator.gte, maxAmount, QueryOperator.lte, null)) {
                // 查询最大调单卡号
                adjustCards = queryMaxAdjustCards(request.getCaseId(), minAmount, QueryOperator.gte, maxAmount, QueryOperator.lte, null);
            } else {
                // TODO 大于最大查询调单卡号数量
                adjustCards = null;
            }
        } else {
            // 查询卡号的进账、出账记录
            adjustCards = tradeRangeOperationRecord.getAdjustCards();
        }
        if (CollectionUtils.isEmpty(adjustCards)) {
            return ServerResponse.createByErrorMessage("未查询到符合的记录!");
        }
        QuerySpecialParams query = queryParamFactory.queryAdjustCardsTradeRecord(request.getCaseId(), adjustCards, minAmount, maxAmount);
        Page<BankTransactionRecord> page = entranceRepository.findAll(PageRequest.of(pageRequest.getPage(), pageRequest.getPageSize(),
                Sort.Direction.valueOf(sortRequest.getOrder().name()), sortRequest.getProperty()), request.getCaseId(), BankTransactionRecord.class, query);
        if (CollectionUtils.isEmpty(page.getContent())) {
            return ServerResponse.createBySuccess();
        }
        List<BankTransactionRecord> content = page.getContent();
        List<TradeRangeOperationDetailSeeResult> newContent = content.stream().map(this::convertFromTradeRangeOperationDetailSeeResult).collect(Collectors.toList());
        return ServerResponse.createBySuccess(newContent);
    }

    /**
     * <h2> 全部查询 </h2>
     */
    private TradeRangeScreeningDataChartResult selectAllQuery(TradeRangeScreeningDataChartRequest request) {

        // 检查调单卡号的数量
        DateRangeRequest dateRange = request.getDateRange();
        String start = dateRange.getStart() + request.getTimeEnd();
        String end = dateRange.getEnd() + request.getTimeStart();
        if (checkAdjustCardCountByDate(request.getCaseId(), new DateRange(start, end))) {
            // 查询固定最大调单卡号
            List<String> maxAdjustCards = queryMaxAdjustCardsByDate(request.getCaseId(), new DateRange(start, end));
            if (!CollectionUtils.isEmpty(maxAdjustCards)) {
                request.setCardNums(maxAdjustCards);
                return selectIndividualQuery(request);
            }
        } else {
            // TODO

        }
        return new TradeRangeScreeningDataChartResult();
    }

    /**
     * <h2> 获取对应操作记录保存的调单卡号集合(通过id 和 caseId查询) </h2>
     */
    private TradeRangeOperationRecord getOperationRecordsAdjustCardsById(String caseId, String id) {

        String[] queryFields = new String[]{FundTacticsAnalysisField.TradeRangeScreening.ADJUST_CARD, FundTacticsAnalysisField.TradeRangeScreening.MIN_AMOUNT, FundTacticsAnalysisField.TradeRangeScreening.MAX_AMOUNT};
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
     * <h2> 查询固定交易笔数的交易金额 </h2>
     */
    private List<BigDecimal> queryTradeAmounts(TradeRangeScreeningDataChartRequest request, int from, int size) {

        QuerySpecialParams queryTradeAmount = queryParamFactory.queryTradeAmount(request);
        String property = FundTacticsAnalysisField.CHANGE_MONEY;
        Page<BankTransactionRecord> page = entranceRepository.findAll(PageRequest.of(from, size, Sort.Direction.ASC, property),
                request.getCaseId(), BankTransactionRecord.class, queryTradeAmount);
        return mapAmount(page.getContent());
    }

    /**
     * <h2> 查询固定交易笔数的进账金额 </h2>
     */
    private List<BigDecimal> queryCreditAmounts(TradeRangeScreeningDataChartRequest request, int from, int size) {

        QuerySpecialParams queryTradeAmount = queryParamFactory.queryCreditOrPayoutAmount(request, true);
        String property = FundTacticsAnalysisField.CHANGE_MONEY;
        Page<BankTransactionRecord> page = entranceRepository.findAll(PageRequest.of(from, size, Sort.Direction.ASC, property),
                request.getCaseId(), BankTransactionRecord.class, queryTradeAmount);
        return mapAmount(page.getContent());
    }

    /**
     * <h2> 查询固定交易笔数的出账金额 </h2>
     */
    private List<BigDecimal> queryPayoutAmounts(TradeRangeScreeningDataChartRequest request, int from, int size) {

        QuerySpecialParams queryTradeAmount = queryParamFactory.queryCreditOrPayoutAmount(request, false);
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
        operationRecord.setAccountOpeningIDNumber(request.getAccountOpeningNumber());
        operationRecord.setAccountOpeningName(request.getAccountOpeningName());
        if (!CollectionUtils.isEmpty(request.getCardNum())) {
            operationRecord.setAdjustCards(request.getCardNum());
            operationRecord.setIndividualBankCardsNumber(request.getCardNum().size());
        } else {
            // 代表全部查询
            operationRecord.setIndividualBankCardsNumber(Integer.MAX_VALUE - 1);
        }
        operationRecord.setDataCategory(request.getSaveCateGory());
        operationRecord.setOperationPeople(request.getOperationPeople());
        operationRecord.setMinAmount(request.getMinAmount());
        operationRecord.setMaxAmount(request.getMaxAmount());
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
}
