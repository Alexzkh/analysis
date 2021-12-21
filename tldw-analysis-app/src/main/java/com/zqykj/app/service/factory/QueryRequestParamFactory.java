/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory;

import com.zqykj.parameters.query.CombinationQueryParams;
import com.zqykj.parameters.query.QuerySpecialParams;
import org.springframework.lang.Nullable;

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

    <T, V> QuerySpecialParams createTradeStatisticalAnalysisQueryRequestByMainCards(T request, V other, Class<?> queryTable);

    /**
     * <h2> 构建交易统计分析聚合展示字段查询 </h2>
     */
    QuerySpecialParams buildTradeStatisticalAnalysisHitsQuery(List<String> queryCards, String caseId);

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
     * <h2> 构建交易汇聚分析结果查询请求(基于选中一组调单卡号集合为查询条件) </h2>
     */
    <T, V> QuerySpecialParams buildTradeConvergenceAnalysisResultMainCardsRequest(T request, V other);

    /**
     * <h2> 构建交易汇聚分析聚合展示字段查询 </h2>
     */
    QuerySpecialParams buildTradeConvergenceAnalysisHitsQuery(List<String> mergeCards, String caseId);

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
     * 新
     */

    /**
     * <h2> 通过查询卡号获取进出记录 </h2>
     * <p>
     * 其中cards 卡号作为查询卡号
     */
    QuerySpecialParams getInoutRecordsViaAdjustCards(List<String> cards, String caseId, int singleQuota, boolean isIn);

    /**
     * <h2> 通过查询卡号与对方卡号 获取进出记录 </h2>
     * <p>
     * 其中cards 卡号作为查询卡号
     */
    QuerySpecialParams getInoutRecordsViaQueryAndOpposite(List<String> cards, @Nullable List<String> oppositeCards, String caseId, int singleQuota, boolean isIn);

    /**
     * <h2> 查询调单卡号 </h2>
     * <p>
     * 过滤条件为交易金额、案件Id、查询的表是{@link com.zqykj.domain.bank.BankTransactionFlow}
     */
    QuerySpecialParams getAdjustCards(String caseId, int singleQuota);

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
}
