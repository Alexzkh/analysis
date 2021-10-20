/**
 * @作者 Mcj
 */
package com.zqykj.factory;

import com.zqykj.parameters.aggregate.AggregationParams;

/**
 * <h1> 公共聚合请求参数构建工厂 </h1>
 */
public interface AggregationRequestParamFactory {

    /**
     * @param request: 构建交易统计分析查询聚合请求体.
     * @return: com.zqykj.parameters.aggregate.AggregationParams
     **/
    <T> AggregationParams createTradeStatisticsAnalysisQueryAgg(T request);

    /**
     * @param request: 构建资产趋势分析查询请求体.
     * @return: com.zqykj.parameters.aggregate.AggregationParams
     **/
    <T> AggregationParams createAssetTrendsAnalysisQueryAgg(T request);

    /**
     * <h2> 获取去重后的总数据量 </h2>
     */
    <T> AggregationParams getTotal(T field);
}
