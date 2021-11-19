/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory;

import com.zqykj.parameters.query.CombinationQueryParams;
import com.zqykj.parameters.query.QuerySpecialParams;

import java.util.List;

/**
 * <h1> 公共查询请求参数构建工厂 </h1>
 */
public interface QueryRequestParamFactory {

    /**
     * 构建交易统计时间规律的构建.
     *
     * @param request: 前置请求body.T->TradeStatisticalAnalysisPreRequest
     * @param other:   案件编号
     * @return: com.zqykj.parameters.query.QuerySpecialParams
     **/
    <T, V> QuerySpecialParams createTradeAmountByTimeQuery(T request, V other);

    <T, V> QuerySpecialParams createTradeStatisticalAnalysisQueryRequestByMainCards(T request, V other);

    /**
     * 构建公共查询请求体.
     *
     * @param request:   前置请求body.T->TradeStatisticalAnalysisPreRequest
     * @param parameter: 案件编号
     * @return: com.zqykj.parameters.query.CombinationQueryParams
     **/
    <T, V> CombinationQueryParams buildCommonQueryParamsViaBankTransactionFlow(T request, V parameter);

    <T, V> CombinationQueryParams buildCommonQueryParamsViaBankTransactionRecord(T request, V parameter);

    /**
     * 组装本方模糊查询请求参数.
     *
     * @param keyword: 模糊查询参数.
     * @return: com.zqykj.parameters.query.CombinationQueryParams
     **/
    CombinationQueryParams assembleLocalFuzzy(String keyword);

    /**
     * 组装对方模糊查询请求参数.
     *
     * @param keyword: 模糊查询参数.
     * @return: com.zqykj.parameters.query.CombinationQueryParams
     **/
    CombinationQueryParams assembleOppositeFuzzy(String keyword);

    /**
     * 构建人员地域分析查询参数.
     *
     * @param requestParam:人员地域请求body. T->PeopleAreaRequest
     * @param parameter:案件编号
     * @return: com.zqykj.parameters.query.QuerySpecialParams
     **/
    <T, V> QuerySpecialParams bulidPeopleAreaAnalysisRequest(T requestParam, V parameter);


    /**
     * 构建人员地域详情分析查询参数.
     *
     * @param requestParam:人员地域请求body. T->PeopleAreaDetailRequest
     * @param parameter:案件编号
     * @return: com.zqykj.parameters.query.QuerySpecialParams
     **/
    <T, V> QuerySpecialParams bulidPeopleAreaDetailAnalysisRequest(T requestParam, V parameter);

    /**
     * 构建资金来源去向es前置查询参数.
     *
     * @param requestParam: 资金来源去向body. T -> FundsSourceAndDestinationStatistisRequest.
     * @param parameter:    案件编号.
     * @return: com.zqykj.parameters.query.QuerySpecialParams
     **/
    <T, V> QuerySpecialParams buildFundsSourceAndDestinationAnalysisResquest(T requestParam, V parameter);


    /**
     * <h2> 构建交易汇聚分析结果查询请求(基于选中一组调单卡号集合为查询条件) </h2>
     */
    <T, V> QuerySpecialParams buildTradeConvergenceAnalysisResultMainCardsRequest(T request, V other);

    /**
     * <h2> 构建最基本查询参数请求 </h2>
     * <p>
     * 案件域
     */
    <T, V> QuerySpecialParams buildBasicParamQueryViaCase(T request, V other);

    /**
     * <h2> 给定一组卡号集合, 筛选出调单的 </h2>
     */
    <T, V> QuerySpecialParams filterMainCards(T request, V other, List<String> cards);

    /**
     * 构建单卡画像查询参数
     */
    <T> QuerySpecialParams buildSingleCardPortraitQueryParams(T request);
}
