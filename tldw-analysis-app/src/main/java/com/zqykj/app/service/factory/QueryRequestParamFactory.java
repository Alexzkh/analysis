/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory;

import com.zqykj.app.service.vo.fund.FundTacticsPartGeneralRequest;
import com.zqykj.parameters.query.CombinationQueryParams;
import com.zqykj.parameters.query.DateRange;
import com.zqykj.parameters.query.QueryOperator;
import com.zqykj.parameters.query.QuerySpecialParams;

import java.util.List;

/**
 * <h1> 公共查询请求参数构建工厂 </h1>
 */
public interface QueryRequestParamFactory {

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
     * <h2> 构建最基本查询参数请求 </h2>
     * <p>
     * 案件域
     */
    <T, V> QuerySpecialParams buildBasicParamQueryViaCase(T request, V other);

    /**
     * <h2> 给定一组卡号集合, 筛选出调单的 </h2>
     */
    QuerySpecialParams filterMainCards(String caseId, List<String> cards);

    /**
     * 构建单卡画像查询参数
     */
    <T> QuerySpecialParams buildSingleCardPortraitQueryParams(T request);

    /**
     * 构建选择个人查询参数
     */
    <T> QuerySpecialParams buildAdjustIndividualQuery(T request);

    /**
     * 构建单卡画像-基本信息和统计查询参数
     *
     * @param request 单卡画像-基本信息和统计请求体
     * @param <T>     T
     * @return 返回构建的查询参数
     */
    <T> QuerySpecialParams buildIndividualInfoAndStatisticsQueryParams(T request);

    /**
     * 构建单卡画像-名下卡交易统计查询参数
     *
     * @param request 单卡画像-名下卡交易统计请求体
     * @param <T>     T
     * @return 返回构建的查询参数
     */
    <T> QuerySpecialParams buildIndividualCardTransactionStatisticsQueryParams(T request);


    QuerySpecialParams queryDataByCaseId(String caseId);

    QuerySpecialParams queryByIdAndCaseId(String caseId, String id);

    /**
     * <h2> 查询调单卡号 </h2>
     * <p>
     * 过滤条件为交易金额、案件Id、交易日期时间 查询的表是{@link com.zqykj.domain.bank.BankTransactionFlow}
     */
    QuerySpecialParams queryAdjustNumberByAmountAndDate(String caseId, Double startAmount, QueryOperator startOperator,
                                                        Double endAmount, QueryOperator endOperator, DateRange dateRange);

    QuerySpecialParams queryAdjustNumberByAmountAndDate(String caseId, Double startAmount, QueryOperator startOperator, DateRange dateRange);

    QuerySpecialParams queryAdjustNumberByDate(String caseId, DateRange dateRange);

    /**
     * <h2> 查询资金交易详情 </h2>
     */
    QuerySpecialParams queryTradeAnalysisDetail(FundTacticsPartGeneralRequest request, String... detailFuzzyFields);
}
