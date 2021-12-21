/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory.requestparam.agg;

import com.zqykj.parameters.aggregate.AggregationParams;

/**
 * <h1> 交易统计分析聚合请求参数构建工厂 </h1>
 */
public interface TradeStatisticalAnalysisAggParamFactory {

    /**
     * <h2> 构建获取交易统计分析根据时间类型获取结果 聚合参数 </h2>
     **/
    <T> AggregationParams buildTradeStatisticsAnalysisFundByTimeType(T request);

    /**
     * <h2> 构建交易统计分析结果查询参数(用户明确给定一组调单卡号集合) 、全部查询不适用此方法 </h2>
     */
    <T> AggregationParams buildTradeStatisticsAnalysisByMainCards(T request, int from, int size);

    /**
     * <h2> 构建获取交易统计分析总数的聚合参数 </h2>
     **/
    <T> AggregationParams buildTradeStatisticsAnalysisTotalAgg(T request);

    /**
     * <h2> 构建交易统计分析结果查询参数(用户明确给定一组调单卡号集合、只返回查询卡号) </h2>
     */
    <T> AggregationParams buildTradeStatisticalQueryCardsAgg(T request, int from, int size);

    /**
     * <h2> 构建交易统计分析聚合展示字段聚合 </h2>
     */
    AggregationParams buildTradeStatisticalAnalysisHitsAgg(int groupSize);
}
