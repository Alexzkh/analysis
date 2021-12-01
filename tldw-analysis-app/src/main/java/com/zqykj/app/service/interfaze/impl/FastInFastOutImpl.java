/**
 * @作者 Mcj
 */
package com.zqykj.app.service.interfaze.impl;

import com.zqykj.app.service.factory.AggregationEntityMappingFactory;
import com.zqykj.app.service.factory.AggregationRequestParamFactory;
import com.zqykj.app.service.factory.AggregationResultEntityParseFactory;
import com.zqykj.app.service.factory.QueryRequestParamFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.interfaze.IFastInFastOut;
import com.zqykj.app.service.interfaze.IFundTacticsAnalysis;
import com.zqykj.app.service.vo.fund.FastInFastOutRequest;
import com.zqykj.app.service.vo.fund.FastInFastOutResult;
import com.zqykj.app.service.vo.fund.middle.CreditAdjustCards;
import com.zqykj.builder.QueryParamsBuilders;
import com.zqykj.common.core.ServerResponse;
import com.zqykj.common.enums.ConditionType;
import com.zqykj.domain.Page;
import com.zqykj.domain.PageRequest;
import com.zqykj.domain.bank.BankTransactionRecord;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.query.CombinationQueryParams;
import com.zqykj.parameters.query.QueryOperator;
import com.zqykj.parameters.query.QuerySpecialParams;
import com.zqykj.repository.EntranceRepository;
import com.zqykj.util.BigDecimalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <h1> 战法快进快出 </h1>
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class FastInFastOutImpl implements IFastInFastOut {

    private final EntranceRepository entranceRepository;

    private final AggregationRequestParamFactory aggregationRequestParamFactory;

    private final QueryRequestParamFactory queryRequestParamFactory;

    private final AggregationEntityMappingFactory aggregationEntityMappingFactory;

    private final AggregationResultEntityParseFactory aggregationResultEntityParseFactory;

    private final IFundTacticsAnalysis fundTacticsAnalysis;

    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Value("${buckets.page.initSize}")
    private int initGroupSize;

    @Value("${global.chunkSize}")
    private int globalChunkSize;

    @Value("${chunkSize}")
    private int chunkSize;

    public ServerResponse fastInFastOutAnalysis(FastInFastOutRequest request) {

        // 返回调单卡号为来源卡号情况的结果数据
        List<FastInFastOutResult> fundSourceResult = processAdjustCardAsFundSourceCard();
        // 返回调单卡号为中转卡号情况的结果数据
        List<FastInFastOutResult> fundTransitResult = processAdjustCardAsFundTransitCard(request);
        // 返回调单卡号为沉淀卡号情况的结果数据
        List<FastInFastOutResult> fundDepositResult = processAdjustCardAsFundDepositCard();


        return ServerResponse.createBySuccess();
    }


    /**
     * <h2> 处理调单卡号为资金来源卡号的情况 </h2>
     */
    private List<FastInFastOutResult> processAdjustCardAsFundSourceCard() {

        return null;
    }

    /**
     * <h2> 处理调单卡号为资金中转卡号的情况 </h2>
     */
    private List<FastInFastOutResult> processAdjustCardAsFundTransitCard(FastInFastOutRequest request) {
        return null;
    }

    /**
     * <h2> 处理调单卡号为资金沉淀卡号的情况 </h2>
     */
    private List<FastInFastOutResult> processAdjustCardAsFundDepositCard() {

        return null;
    }


    /**
     * <h2> 获取入账的调单卡号集合(中转卡号情况) </h2>
     * <p>
     * 处理选择个体情况(返回map)
     *
     * @param caseId      案件Id
     * @param adjustCards 给定的一组调单卡号集合
     * @param singleQuota 单笔限额(指的是交易金额)
     */
    @SuppressWarnings("all")
    private Map<String, CreditAdjustCards> getCreditsAdjustCards(String caseId, List<String> adjustCards, int singleQuota,
                                                                 int from, int size) {

        // 构建查询入账调单卡号查询请求参数
        QuerySpecialParams query = queryRequestParamFactory.buildCreditsAdjustCards(caseId, adjustCards, singleQuota);
        // 构建查询入账调单卡号聚合请求参数
        AggregationParams agg = aggregationRequestParamFactory.buildCreditsAdjustCardsAgg(initGroupSize, from, size);
        // 构建聚合名称属性映射(获取聚合值)
        Map<String, String> aggNameKeyMapping = new LinkedHashMap<>();
        aggregationEntityMappingFactory.buildTradeAnalysisResultAggMapping(aggNameKeyMapping, CreditAdjustCards.class);
        // 设置此聚合功能名称
        agg.setResultName("getCreditsAdjustCards");
        Map<String, List<List<Object>>> resultsMap = entranceRepository.compoundQueryAndAgg(query, agg, BankTransactionRecord.class, caseId);
        List<List<Object>> results = resultsMap.get(agg.getResultName());
        return results.stream().collect(Collectors.toMap(e -> e.get(0).toString(),
                e1 -> new CreditAdjustCards(e1.get(0).toString(), Integer.parseInt(e1.get(1).toString()), BigDecimalUtil.value(e1.get(2).toString())),
                (v1, v2) -> v1
        ));
    }

    /**
     * <h2> 获取入账的调单卡号集合(中转卡号情况) </h2>
     * <p>
     * 处理全部查询情况(返回list)
     *
     * @param caseId      案件Id
     * @param adjustCards 给定的一组调单卡号集合
     * @param singleQuota 单笔限额(指的是交易金额)
     */
    @SuppressWarnings("all")
    private List<CreditAdjustCards> getgetCreditsAdjustCardsList(String caseId, List<String> adjustCards, int singleQuota,
                                                                 int from, int size) {

        // 构建查询入账调单卡号查询请求参数
        QuerySpecialParams query = queryRequestParamFactory.buildCreditsAdjustCards(caseId, adjustCards, singleQuota);
        // 构建查询入账调单卡号聚合请求参数
        AggregationParams agg = aggregationRequestParamFactory.buildCreditsAdjustCardsAgg(initGroupSize, from, size);
        // 构建聚合名称属性映射(获取聚合值)
        Map<String, String> aggNameKeyMapping = new LinkedHashMap<>();
        aggregationEntityMappingFactory.buildTradeAnalysisResultAggMapping(aggNameKeyMapping, CreditAdjustCards.class);
        // 设置此聚合功能名称
        agg.setResultName("getCreditsAdjustCards");
        Map<String, List<List<Object>>> resultsMap = entranceRepository.compoundQueryAndAgg(query, agg, BankTransactionRecord.class, caseId);
        List<List<Object>> results = resultsMap.get(agg.getResultName());
        return results.stream().map(e -> new CreditAdjustCards(e.get(0).toString(), Integer.parseInt(e.get(1).toString()), BigDecimalUtil.value(e.get(2).toString()))).collect(Collectors.toList());
    }

    /**
     * <h2> 获取入账的调单卡号总数量 </h2>
     */
    public long getCreditsAdjustCardsTotal(String caseId, List<String> adjustCards, int singleQuota) {

        // 构建查询入账调单卡号查询请求参数
        QuerySpecialParams query = queryRequestParamFactory.buildCreditsAdjustCards(caseId, adjustCards, singleQuota);
        // 聚合查询参数
        AggregationParams agg = aggregationRequestParamFactory.buildCreditsAdjustCardsTotalAgg();
        // 构建聚合名称属性映射(获取聚合值)
        Map<String, String> mapping = aggregationEntityMappingFactory.buildDistinctTotalAggMapping(FundTacticsAnalysisField.QUERY_CARD);
        agg.setMapping(mapping);
        // 设置此聚合功能名称
        agg.setResultName("total");
        Map<String, List<List<Object>>> resultsMap = entranceRepository.compoundQueryAndAgg(query, agg, BankTransactionRecord.class, caseId);
        if (CollectionUtils.isEmpty(resultsMap)) {
            return 0;
        } else {
            List<List<Object>> total = resultsMap.get("total");
            if (CollectionUtils.isEmpty(total)) {
                return 0;
            } else {
                return Long.parseLong(total.get(0).get(0).toString());
            }
        }
    }

    /**
     * <h2> 计算单一来源到多个沉淀 </h2>
     *
     * @param creditCard 卡号的入账情况
     * @param request    快进快出请求参数
     * @param cards      给定一组卡号集合
     */
    private List<FastInFastOutResult> multiDepositFromSingleSource(CreditAdjustCards creditCard, FastInFastOutRequest request, List<String> cards) {

        // 案件Id
        String caseId = request.getCaseId();
        // 单笔限额
        int singleQuota = request.getSingleQuota();
        // 特征比
        int characteristicRatio = request.getCharacteristicRatio();
        // 时间间隔(分钟)
        int timeInterval = request.getTimeInterval();
        // TODO page,size 需要等待处理
        // cards的入账数据
        List<BankTransactionRecord> creditsRecords = getCreditsAndPayOutDataViaCards(caseId, cards, singleQuota, 0, 25, true);
        // cards的出账数据
        List<BankTransactionRecord> payoutRecords = getCreditsAndPayOutDataViaCards(caseId, cards, singleQuota, 0, 25, false);
        // 计算特征比、计算时间间隔
        // 先找出入账的数据
        BankTransactionRecord creditData = creditsRecords.stream().filter(card -> card.getQueryCard().equals(creditCard.getAdjustCard())).findFirst().orElse(null);
        if (null == creditData) {
            // TODO 等待处理
            return null;
        }
        final BigDecimal[] payoutAmountSum = {new BigDecimal("0")};
        // 筛选符合条件的出账数据
        if (!CollectionUtils.isEmpty(payoutRecords)) {
            return payoutRecords.stream().filter(payoutData -> {
                payoutAmountSum[0] = BigDecimalUtil.add(payoutAmountSum[0].doubleValue(), payoutData.getChangeAmount());
                // 出账金额累加不能大于入账金额
                if (payoutAmountSum[0].compareTo(creditCard.getCreditsAmount()) > 0) {
                    return false;
                }
                return checkFeatureRatio(characteristicRatio, creditData, payoutData) && checkTimeInterval(timeInterval, creditData, payoutData);
            }).map(payoutData -> {
                // 生成快出快出记录
                return convertFromData(creditData, payoutData);
            }).collect(Collectors.toList());
        }
        return null;
    }


    /**
     * <h2> 获取给定一组卡号入账/出账的数据 </h2>
     * <p>
     * 分页获取数据(返回的数据量大小未知)
     *
     * @param caseId      案件Id
     * @param cards       一组查询卡号集合
     * @param singleQuota 单笔限额(交易金额)
     * @param page        分页页码
     * @param size        分页需要返回的条数
     * @param isCredits   计算入账/出账 (true-入账,false-出账)
     */
    private List<BankTransactionRecord> getCreditsAndPayOutDataViaCards(String caseId, List<String> cards, int singleQuota,
                                                                        int page, int size, boolean isCredits) {

        // 构建查询参数
        QuerySpecialParams querySpecialParams = new QuerySpecialParams();
        CombinationQueryParams filter = new CombinationQueryParams(ConditionType.filter);
        // 调单卡号集合过滤
        if (!CollectionUtils.isEmpty(cards)) {
            filter.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.QUERY_CARD, cards));
        }
        // 单笔限额过滤
        filter.addCommonQueryParams(QueryParamsBuilders.range(FundTacticsAnalysisField.CHANGE_MONEY, singleQuota, QueryOperator.gte));
        if (isCredits) {
            // 进账
            filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_IN));
        } else {
            // 出账
            filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_OUT));
        }
        querySpecialParams.addCombiningQueryParams(filter);
        Page<BankTransactionRecord> recordPage = entranceRepository.findAll(PageRequest.of(page, size), caseId, BankTransactionRecord.class, querySpecialParams);
        if (null == recordPage) {
            return new ArrayList<>();
        }
        return recordPage.getContent();
    }

    /**
     * <h2> 检查特征比 </h2>
     */
    private boolean checkFeatureRatio(int characteristicRatio, BankTransactionRecord first, BankTransactionRecord second) {

        // 特征比(入账金额-出账金额) / 入账金额
        BigDecimal sub = BigDecimalUtil.sub(first.getChangeAmount(), first.getChangeAmount());
        BigDecimal div = BigDecimalUtil.div(sub.doubleValue(), first.getChangeAmount());
        return div.intValue() <= characteristicRatio;
    }


    /**
     * <h2> 检查时间间隔 </h2>
     * <p>
     * 查看流入时间日期 与 流出时间日期之前必须 小于等于timeInterval
     */
    private boolean checkTimeInterval(int timeInterval, BankTransactionRecord first, BankTransactionRecord second) {
        // 如果 流出时间日期 早于 流入时间日期(过滤此数据)
        if (second.getTradingTime().before(first.getTradingTime())) {
            return false;
        }
        // 查看时间间隔
        Calendar firstCal = Calendar.getInstance();
        firstCal.setTime(first.getTradingTime());
        Calendar secondCal = Calendar.getInstance();
        secondCal.setTime(first.getTradingTime());
        int firstMinute = firstCal.get(Calendar.MINUTE);
        int secondMinute = secondCal.get(Calendar.MINUTE);
        return (secondMinute - firstMinute) <= timeInterval;
    }

    private FastInFastOutResult convertFromData(BankTransactionRecord credit, BankTransactionRecord payout) {

        FastInFastOutResult fastInFastOutResult = new FastInFastOutResult();
        // 资源来源卡号
        fastInFastOutResult.setFundSourceCard(credit.getQueryCard());
        // 资金来源户名
        fastInFastOutResult.setFundSourceAccountName(credit.getCustomerName());
        // 流入时间日期
        fastInFastOutResult.setInflowDate(format.format(credit.getTradingTime()));
        // 流入金额
        fastInFastOutResult.setInflowAmount(BigDecimalUtil.value(credit.getChangeAmount().toString()));
        // 资金中转卡号
        fastInFastOutResult.setFundTransitCard(payout.getQueryCard());
        // 资金中转户名
        fastInFastOutResult.setFundTransitAccountName(payout.getCustomerName());
        // 流出时间日期
        fastInFastOutResult.setOutflowDate(format.format(payout.getTradingTime()));
        // 流出金额
        fastInFastOutResult.setOutflowAmount(BigDecimalUtil.value(payout.getChangeAmount().toString()));
        // 资金沉淀卡号
        fastInFastOutResult.setFundDepositCard(payout.getTransactionOppositeCard());
        // 资金沉淀户名
        fastInFastOutResult.setFundDepositAccountName(payout.getTransactionOppositeName());
        // 特征比(入账金额-出账金额) / 入账金额
        BigDecimal sub = BigDecimalUtil.sub(credit.getChangeAmount(), payout.getChangeAmount());
        BigDecimal div = BigDecimalUtil.div(sub.doubleValue(), credit.getChangeAmount());
        fastInFastOutResult.setCharacteristicRatio(div.toString() + "%");
        return fastInFastOutResult;
    }
}
