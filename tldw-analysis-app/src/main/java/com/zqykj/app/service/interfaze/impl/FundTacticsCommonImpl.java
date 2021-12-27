/**
 * @作者 Mcj
 */
package com.zqykj.app.service.interfaze.impl;

import com.zqykj.app.service.factory.AggregationEntityMappingFactory;
import com.zqykj.app.service.factory.AggregationRequestParamFactory;
import com.zqykj.app.service.factory.QueryRequestParamFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.domain.bank.BankTransactionFlow;
import com.zqykj.parameters.Pagination;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.query.DateRange;
import com.zqykj.parameters.query.QueryOperator;
import com.zqykj.parameters.query.QuerySpecialParams;
import com.zqykj.repository.EntranceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
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
    // 最大查询调单卡号数量
    @Value("${fundTactics.queryAll.max_adjustCard_query_count}")
    protected int maxAdjustCardQueryCount;

    protected static final String CARDINALITY_TOTAL = "cardinality_total";

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
        AggregationParams aggregationParams = aggParamFactory.groupByAndCountField(FundTacticsAnalysisField.QUERY_CARD, maxAdjustCardQueryCount, new Pagination(0, maxAdjustCardQueryCount));
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
        return count <= maxAdjustCardQueryCount;
    }
}
