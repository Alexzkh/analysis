/**
 * @作者 Mcj
 */
package com.zqykj.app.service.interfaze.impl;

import com.zqykj.app.service.factory.AggregationEntityMappingFactory;
import com.zqykj.app.service.factory.param.agg.TradeRangeScreeningAggParamFactory;
import com.zqykj.app.service.factory.param.query.TradeRangeScreeningQueryParamFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.interfaze.ITradeRangeScreening;
import com.zqykj.app.service.vo.fund.TradeRangeScreeningDataChartRequest;
import com.zqykj.app.service.vo.fund.TradeRangeScreeningDataChartResult;
import com.zqykj.app.service.vo.fund.TradeRangeScreeningSaveRequest;
import com.zqykj.common.core.ServerResponse;
import com.zqykj.domain.Page;
import com.zqykj.domain.PageRequest;
import com.zqykj.domain.Sort;
import com.zqykj.domain.bank.BankTransactionFlow;
import com.zqykj.domain.bank.BankTransactionRecord;
import com.zqykj.domain.bank.TradeRangeOperationRecord;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.query.QuerySpecialParams;
import com.zqykj.repository.EntranceRepository;
import com.zqykj.util.BigDecimalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <h1> 交易区间筛选 </h1>
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TradeRangeScreeningImpl implements ITradeRangeScreening {

    private final EntranceRepository entranceRepository;
    private final TradeRangeScreeningQueryParamFactory queryParamFactory;
    private final TradeRangeScreeningAggParamFactory aggParamFactory;
    private final AggregationEntityMappingFactory entityMappingFactory;

    // 最大查询调单卡号数量
    @Value("${fundTactics.max_adjustCard_query_count}")
    private int maxAdjustCardQueryCount;

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
        entranceRepository.save(operationRecord, request.getCaseId(), TradeRangeOperationRecord.class);
        return ServerResponse.createBySuccess("成功");
    }

    /**
     * <h2> 全部查询 </h2>
     */
    private TradeRangeScreeningDataChartResult selectAllQuery(TradeRangeScreeningDataChartRequest request) {

        // 检查调单卡号的数量
        if (checkAdjustCardCount(request.getCaseId())) {
            // 查询固定最大调单卡号
            List<String> maxAdjustCards = queryMaxAdjustCards(request.getCaseId());
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
     * <h2> 检查调单卡号数量 </h2>
     */
    private boolean checkAdjustCardCount(String caseId) {

        QuerySpecialParams querySpecialParams = queryParamFactory.queryCase(caseId);
        AggregationParams aggregationParams = aggParamFactory.queryAdjustCardTotal();
        aggregationParams.setMapping(entityMappingFactory.buildDistinctTotalAggMapping(FundTacticsAnalysisField.QUERY_CARD));
        aggregationParams.setResultName("adjustCardTotal");
        Map<String, List<List<Object>>> compoundQueryAndAgg = entranceRepository.compoundQueryAndAgg(querySpecialParams, aggregationParams, BankTransactionFlow.class, caseId);
        List<List<Object>> total = compoundQueryAndAgg.get(aggregationParams.getResultName());
        if (CollectionUtils.isEmpty(total)) {
            return true;
        }
        long count = Long.parseLong(total.get(0).get(0).toString());
        return count <= maxAdjustCardQueryCount;
    }

    /**
     * <h2> 查询最大调单卡号 </h2>
     */
    private List<String> queryMaxAdjustCards(String caseId) {

        QuerySpecialParams querySpecialParams = queryParamFactory.queryCase(caseId);
        AggregationParams aggregationParams = aggParamFactory.queryFixedCountAdjustCards(maxAdjustCardQueryCount);
        aggregationParams.setMapping(entityMappingFactory.buildGroupByAggMapping(FundTacticsAnalysisField.QUERY_CARD));
        aggregationParams.setResultName("adjustCards");
        Map<String, List<List<Object>>> compoundQueryAndAgg = entranceRepository.compoundQueryAndAgg(querySpecialParams, aggregationParams, BankTransactionFlow.class, caseId);
        List<List<Object>> cards = compoundQueryAndAgg.get(aggregationParams.getResultName());
        if (CollectionUtils.isEmpty(cards)) {
            return null;
        }
        return cards.stream().map(e -> e.get(0).toString()).collect(Collectors.toList());
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
        operationRecord.setAccountIDNumber(request.getAccountOpeningNumber());
        operationRecord.setAccountName(request.getAccountOpeningName());
        if (!CollectionUtils.isEmpty(request.getCardNum())) {
            operationRecord.setAdjustCards(request.getCardNum());
            operationRecord.setIndividualBankCardsNumber(request.getCardNum().size());
        } else {
            // 代表全部查询
            operationRecord.setIndividualBankCardsNumber(Integer.MAX_VALUE - 1);
        }
        operationRecord.setDataCateGory(request.getSaveCateGory());
        operationRecord.setOperationPeople(request.getOperationPeople());
        operationRecord.setMinAmount(request.getMinAmount());
        operationRecord.setMaxAmount(request.getMaxAmount());
        operationRecord.setRemark(request.getRemark());
        operationRecord.setOperationDate(new Date());
        return operationRecord;
    }
}
