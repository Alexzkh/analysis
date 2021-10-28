/**
 * @作者 Mcj
 */
package com.zqykj.factory;

import com.zqykj.parameters.query.QuerySpecialParams;

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

    /**
     * 构建交易统计分析查询参数.
     *
     * @param request: 交易统计分析请求body.T->TradeStatisticalAnalysisQueryRequest
     * @param other:   案件编号
     * @return: com.zqykj.parameters.query.QuerySpecialParams
     **/
    <T, V> QuerySpecialParams createTradeStatisticalAnalysisQueryRequest(T request, V other);

    /**
     * 构建交易统计公共请求参数.
     *
     * @param request:   前置请求body.T->TradeStatisticalAnalysisPreRequest
     * @param parameter: 案件编号
     * @return: com.zqykj.parameters.query.QuerySpecialParams
     **/
    <T, V> QuerySpecialParams buildCommonQuerySpecialParams(T request, V parameter);

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
}
