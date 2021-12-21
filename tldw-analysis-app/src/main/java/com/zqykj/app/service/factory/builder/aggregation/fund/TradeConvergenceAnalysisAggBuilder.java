/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory.builder.aggregation.fund;

import com.zqykj.app.service.factory.QueryRequestParamFactory;
import com.zqykj.app.service.factory.requestparam.agg.TradeConvergenceAnalysisAggParamFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.vo.fund.TradeConvergenceAnalysisQueryRequest;
import com.zqykj.builder.AggregationParamsBuilders;
import com.zqykj.common.enums.ConditionType;
import com.zqykj.enums.AggsType;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.aggregate.pipeline.PipelineAggregationParams;
import com.zqykj.parameters.query.CombinationQueryParams;
import com.zqykj.parameters.query.CommonQueryParams;
import com.zqykj.parameters.query.QuerySpecialParams;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * <h1> 交易汇聚分析聚合参数构建 </h1>
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TradeConvergenceAnalysisAggBuilder extends FundTacticsPartCommonAgg implements TradeConvergenceAnalysisAggParamFactory {

    private final QueryRequestParamFactory queryRequestParamFactory;

    @Override
    public <T> AggregationParams buildTradeConvergenceAnalysisResultMainCardsAgg(T request, int from, int size) {

        TradeConvergenceAnalysisQueryRequest convergenceRequest = (TradeConvergenceAnalysisQueryRequest) request;

        AggregationParams cardTerms = AggregationParamsBuilders.terms("local_card_terms", FundTacticsAnalysisField.MERGE_CARD);
        cardTerms.setSize(convergenceRequest.getGroupInitSize());
        cardTerms.setCollectMode("DEPTH_FIRST");
        // 交易总次数
        AggregationParams tradeTotalTimes = AggregationParamsBuilders.count("local_trade_total",
                FundTacticsAnalysisField.MERGE_CARD, null);

        cardTerms.setPerSubAggregation(tradeTotalTimes);
        fundTacticsPartUniversalAgg(cardTerms, convergenceRequest);
        // 设置分页 与 排序
        // 排序
        PipelineAggregationParams sort = fundTacticsPartUniversalAggSort(convergenceRequest.getSortRequest(), from, size);
        if (null != sort) {
            cardTerms.setPerSubAggregation(sort);
        }
        return cardTerms;
    }

    @Override
    public <T> AggregationParams buildTradeConvergenceQueryAndMergeCardsAgg(T request, int from, int size) {
        TradeConvergenceAnalysisQueryRequest convergenceRequest = (TradeConvergenceAnalysisQueryRequest) request;

        AggregationParams cardTerms = AggregationParamsBuilders.terms("groupBy_" + FundTacticsAnalysisField.MERGE_CARD, FundTacticsAnalysisField.MERGE_CARD);
        cardTerms.setSize(convergenceRequest.getGroupInitSize());
        cardTerms.setCollectMode("DEPTH_FIRST");
        // 交易总次数
        AggregationParams tradeTotalTimes = AggregationParamsBuilders.count("local_trade_total",
                FundTacticsAnalysisField.MERGE_CARD, null);

        cardTerms.setPerSubAggregation(tradeTotalTimes);
        fundTacticsPartUniversalAgg(cardTerms, convergenceRequest);
        // 设置分页 与 排序
        // 排序
        PipelineAggregationParams sort = fundTacticsPartUniversalAggSort(convergenceRequest.getSortRequest(), from, size);
        if (null != sort) {
            cardTerms.setPerSubAggregation(sort);
        }
        return cardTerms;
    }

    @Override
    public <T> AggregationParams buildTradeConvergenceAnalysisResultTotalAgg(T request) {

        TradeConvergenceAnalysisQueryRequest queryRequest = (TradeConvergenceAnalysisQueryRequest) request;

        AggregationParams filter = null;
        // 前置过滤条件
        CombinationQueryParams combinationOne = new CombinationQueryParams();
        combinationOne.setType(ConditionType.filter);
        // 需要的模糊匹配
        if (StringUtils.isNotBlank(queryRequest.getKeyword())) {
            CombinationQueryParams localFuzzy = queryRequestParamFactory.assembleLocalFuzzy(queryRequest.getKeyword());
            CombinationQueryParams oppositeFuzzy = queryRequestParamFactory.assembleOppositeFuzzy(queryRequest.getKeyword());
            localFuzzy.getCommonQueryParams().addAll(oppositeFuzzy.getCommonQueryParams());
            combinationOne.addCommonQueryParams(new CommonQueryParams(localFuzzy));
        }
        if (!CollectionUtils.isEmpty(combinationOne.getCommonQueryParams())) {
            QuerySpecialParams prefixFilter = new QuerySpecialParams();
            prefixFilter.addCombiningQueryParams(combinationOne);
            filter = new AggregationParams("total", AggsType.filter.name(), prefixFilter);
        }
        AggregationParams total = new AggregationParams("distinct_" + FundTacticsAnalysisField.MERGE_CARD, AggsType.cardinality.name(), FundTacticsAnalysisField.MERGE_CARD);

        if (null != filter) {
            filter.setPerSubAggregation(total);
            return filter;
        }
        return total;
    }

    @Override
    public AggregationParams buildTradeConvergenceAnalysisHitsAgg(int groupSize) {

        AggregationParams cardTerms = AggregationParamsBuilders.terms("local_card_terms", FundTacticsAnalysisField.MERGE_CARD);
        cardTerms.setSize(groupSize);
        AggregationParams showFields = fundTacticsPartUniversalAggShowFields(FundTacticsAnalysisField.tradeConvergenceAnalysisShowField(), "local_hits", null);
        cardTerms.setPerSubAggregation(showFields);
        return cardTerms;
    }
}
