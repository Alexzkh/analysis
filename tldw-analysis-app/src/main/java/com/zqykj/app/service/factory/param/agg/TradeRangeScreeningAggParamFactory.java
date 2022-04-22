/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory.param.agg;

import com.zqykj.parameters.aggregate.AggregationParams;

/**
 * <h1> 交易区间筛选聚合参数请求工厂 </h1>
 */
public interface TradeRangeScreeningAggParamFactory {

    AggregationParams individualBankCardsStatisticalAgg(int from, int size, String property, String direction, int groupSize);
}
