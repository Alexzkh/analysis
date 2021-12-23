/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory.param.agg;

import com.zqykj.parameters.aggregate.AggregationParams;

/**
 * <h1> 交易汇聚分析聚合请求参数构建工厂 </h1>
 */
public interface TradeConvergenceAnalysisAggParamFactory {

    /**
     * <h2>  构建交易汇聚分析结果聚合请求(基于选中一组调单卡号集合为查询条件) </h2>
     */
    <T> AggregationParams buildTradeConvergenceAnalysisResultMainCardsAgg(T request, int from, int size);

    /**
     * <h2> 构建交易汇聚分析结果聚合请求(基于选中一组合并卡号集合为查询条件)  </h2>
     */
    <T> AggregationParams buildTradeConvergenceQueryAndMergeCardsAgg(T request, int from, int size);

    /**
     * <h2> 获取交易汇聚分析结果查询总数据量 </h2>
     */
    <T> AggregationParams buildTradeConvergenceAnalysisResultTotalAgg(T request);

    /**
     * <h2> 构建交易汇聚分析聚合展示字段聚合 </h2>
     */
    AggregationParams buildTradeConvergenceAnalysisHitsAgg(int groupSize);
}
