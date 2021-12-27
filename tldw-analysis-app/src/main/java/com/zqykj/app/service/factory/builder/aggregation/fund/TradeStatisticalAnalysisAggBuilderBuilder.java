/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory.builder.aggregation.fund;

import com.zqykj.app.service.factory.QueryRequestParamFactory;
import com.zqykj.app.service.factory.param.agg.TradeStatisticalAnalysisAggParamFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.vo.fund.FundDateRequest;
import com.zqykj.app.service.vo.fund.TradeStatisticalAnalysisQueryRequest;
import com.zqykj.builder.AggregationParamsBuilders;
import com.zqykj.common.enums.ConditionType;
import com.zqykj.enums.AggsType;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.aggregate.date.DateParams;
import com.zqykj.parameters.aggregate.pipeline.PipelineAggregationParams;
import com.zqykj.parameters.query.CombinationQueryParams;
import com.zqykj.parameters.query.CommonQueryParams;
import com.zqykj.parameters.query.QuerySpecialParams;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * <h1> 交易统计分析聚合参数构建 </h1>
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TradeStatisticalAnalysisAggBuilderBuilder extends FundTacticsCommonAggBuilder implements TradeStatisticalAnalysisAggParamFactory {

    private final QueryRequestParamFactory queryRequestParamFactory;

    @Override
    public <T> AggregationParams buildTradeStatisticsAnalysisFundByTimeType(T request) {

        FundDateRequest fundAnalysisDateRequest = (FundDateRequest) request;

        fundAnalysisDateRequest.setDateField(FundTacticsAnalysisField.TRADING_TIME);
        fundAnalysisDateRequest.setMetricsField(FundTacticsAnalysisField.TRANSACTION_MONEY);
        DateParams dateParams = new DateParams();
        // 设置format
        if (StringUtils.isNotBlank(fundAnalysisDateRequest.getFormat())) {
            dateParams.setFormat(fundAnalysisDateRequest.getFormat());
        } else {
            dateParams.setFormat(FundDateRequest.convertFromTimeType(fundAnalysisDateRequest.getTimeType().name()));
        }
        if (fundAnalysisDateRequest.getTimeValue() > 1) {
            // 代表的是固定间隔 fixed
            dateParams.addFixedInterval(fundAnalysisDateRequest.getTimeValue(), fundAnalysisDateRequest.getTimeType().name());
        } else {
            // 默认间隔是1
            dateParams.addCalendarInterval(fundAnalysisDateRequest.getTimeType().name());
        }
        AggregationParams root = new AggregationParams("date_group", AggsType.date_histogram.name(), fundAnalysisDateRequest.getDateField(), dateParams);

        AggregationParams sub = new AggregationParams("trade_amount_sum", AggsType.sum.name(), fundAnalysisDateRequest.getMetricsField());

        root.setPerSubAggregation(sub);

        Map<String, String> bucketsPathMap = new HashMap<>();
        bucketsPathMap.put("final_sum", "trade_amount_sum");
        PipelineAggregationParams pipelineAggregationParams =
                new PipelineAggregationParams("sum_bucket_selector", AggsType.bucket_selector.name(),
                        bucketsPathMap, "params.final_sum > 0");
        root.setPerSubAggregation(pipelineAggregationParams);
        return root;
    }

    @Override
    public <T> AggregationParams buildTradeStatisticsAnalysisByMainCards(T request, int from, int size) {

        TradeStatisticalAnalysisQueryRequest queryRequest = (TradeStatisticalAnalysisQueryRequest) request;

        AggregationParams cardTerms = AggregationParamsBuilders.terms("local_card_terms", FundTacticsAnalysisField.QUERY_CARD);
        cardTerms.setSize(queryRequest.getGroupInitSize());
        cardTerms.setCollectMode("BREADTH_FIRST");
        // 交易总次数
        AggregationParams tradeTotalTimes = AggregationParamsBuilders.count("local_trade_total",
                FundTacticsAnalysisField.QUERY_CARD, null);
        cardTerms.setPerSubAggregation(tradeTotalTimes);
        fundTacticsPartUniversalAgg(cardTerms, queryRequest);
        // 排序
        PipelineAggregationParams sort = fundTacticsPartUniversalAggSort(queryRequest.getSortRequest(), from, size);
        if (null != sort) {
            cardTerms.setPerSubAggregation(sort);
        }
        return cardTerms;
    }

    @Override
    public <T> AggregationParams buildTradeStatisticsAnalysisTotalAgg(T request) {

        TradeStatisticalAnalysisQueryRequest queryRequest = (TradeStatisticalAnalysisQueryRequest) request;

        // 前置过滤条件
        CombinationQueryParams combinationOne = new CombinationQueryParams();
        combinationOne.setType(ConditionType.filter);
        // 本方查询卡号(有可能是查询全部,那么卡号不为空的时候才能选用此条件)
//        if (!CollectionUtils.isEmpty(queryRequest.getCardNums())) {
//            combinationOne.addCommonQueryParams(new CommonQueryParams(QueryType.terms, FundTacticsAnalysisField.QUERY_CARD, queryRequest.getCardNums()));
//        }
        if (StringUtils.isNotBlank(queryRequest.getKeyword())) {
            // 本方需要的模糊匹配
            combinationOne.addCommonQueryParams(new CommonQueryParams(queryRequestParamFactory.assembleLocalFuzzy(queryRequest.getKeyword())));
        }
        QuerySpecialParams prefixFilter = new QuerySpecialParams();
        prefixFilter.addCombiningQueryParams(combinationOne);
        AggregationParams root = new AggregationParams("total", AggsType.filter.name(), prefixFilter);
        AggregationParams total;
        total = new AggregationParams("distinct_" + FundTacticsAnalysisField.QUERY_CARD, AggsType.cardinality.name(), FundTacticsAnalysisField.QUERY_CARD);
        root.setPerSubAggregation(total);
        return root;
    }

    @Override
    public <T> AggregationParams buildTradeStatisticalQueryCardsAgg(T request, int from, int size) {
        TradeStatisticalAnalysisQueryRequest queryRequest = (TradeStatisticalAnalysisQueryRequest) request;

        AggregationParams cardTerms = AggregationParamsBuilders.terms("groupBy_" + FundTacticsAnalysisField.QUERY_CARD, FundTacticsAnalysisField.QUERY_CARD);
        cardTerms.setSize(queryRequest.getGroupInitSize());
        cardTerms.setCollectMode("BREADTH_FIRST");
        // 交易总次数
        AggregationParams tradeTotalTimes = AggregationParamsBuilders.count("local_trade_total",
                FundTacticsAnalysisField.QUERY_CARD, null);
        cardTerms.setPerSubAggregation(tradeTotalTimes);
        fundTacticsPartUniversalAgg(cardTerms, queryRequest);
        // 排序
        PipelineAggregationParams sort = fundTacticsPartUniversalAggSort(queryRequest.getSortRequest(), from, size);
        if (null != sort) {
            cardTerms.setPerSubAggregation(sort);
        }
        return cardTerms;
    }

    @Override
    public AggregationParams buildTradeStatisticalAnalysisHitsAgg(int groupSize) {

        AggregationParams cardTerms = AggregationParamsBuilders.terms("local_card_terms", FundTacticsAnalysisField.QUERY_CARD);
        cardTerms.setSize(groupSize);
        AggregationParams showFields = fundTacticsPartUniversalAggShowFields(FundTacticsAnalysisField.tradeStatisticalAnalysisLocalShowField(), "local_hits", null);
        cardTerms.setPerSubAggregation(showFields);
        return cardTerms;
    }
}
