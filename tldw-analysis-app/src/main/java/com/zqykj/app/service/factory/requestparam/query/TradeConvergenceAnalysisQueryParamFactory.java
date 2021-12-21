/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory.requestparam.query;

import com.zqykj.parameters.query.QuerySpecialParams;

import java.util.List;

/**
 * <h1> 交易汇聚分析查询请求参数构建工厂 </h1>
 */
public interface TradeConvergenceAnalysisQueryParamFactory {

    /**
     * <h2> 构建交易汇聚分析结果查询请求(基于选中一组调单卡号集合为查询条件) </h2>
     */
    <T, V> QuerySpecialParams buildTradeConvergenceAnalysisResultMainCardsRequest(T request, V other);

    /**
     * <h2> 构建交易汇聚分析聚合展示字段查询 </h2>
     */
    QuerySpecialParams buildTradeConvergenceAnalysisHitsQuery(List<String> mergeCards, String caseId);
}
