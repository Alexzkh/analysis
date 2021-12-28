/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory.param.query;

import com.zqykj.parameters.query.QuerySpecialParams;

import java.util.List;

/**
 * <h1> 交易统计分析查询请求参数构建工厂 </h1>
 */
public interface TradeStatisticalAnalysisQueryParamFactory {


    /**
     * <h2> 构建交易统计时间规律的构建 </h2>
     **/
    <T, V> QuerySpecialParams createTradeAmountByTimeQuery(T request, V other);

    /**
     * <h2> 构建交易统计分析查询(调单卡号) </h2>
     **/
    <T, V> QuerySpecialParams createTradeStatisticalAnalysisQueryRequestByMainCards(T request, V other, Class<?> queryTable);

    /**
     * <h2> 构建交易统计分析聚合展示字段查询 </h2>
     */
    QuerySpecialParams buildTradeStatisticalAnalysisHitsQuery(List<String> queryCards, String caseId);
}
