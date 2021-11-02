/**
 * @作者 Mcj
 */
package com.zqykj.factory;

import com.zqykj.parameters.aggregate.AggregationParams;

import java.util.List;

/**
 * <h1> 公共聚合请求参数构建工厂 </h1>
 */
public interface AggregationRequestParamFactory {

    <T> AggregationParams buildTradeStatisticsAnalysisQueryCardAgg(T request);

    <T> AggregationParams buildTradeStatisticsAnalysisOppositeCardAgg(T request);

    <T> AggregationParams buildTradeStatisticsAnalysisTotalAgg(T request);

    /**
     * @param request: 构建资产趋势分析查询请求体.
     * @return: com.zqykj.parameters.aggregate.AggregationParams
     **/
    <T> AggregationParams createAssetTrendsAnalysisQueryAgg(T request);
}
