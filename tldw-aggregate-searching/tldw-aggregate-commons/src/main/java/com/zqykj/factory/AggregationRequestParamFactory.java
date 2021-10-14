/**
 * @作者 Mcj
 */
package com.zqykj.factory;

import com.zqykj.parameters.aggregate.AggregationParams;

/**
 * <h1> 公共聚合请求参数构建工厂 </h1>
 */
public interface AggregationRequestParamFactory {

    <T> AggregationParams createTradeStatisticsAnalysisQueryAgg(T request);
}
