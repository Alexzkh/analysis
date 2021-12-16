/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory;

import com.zqykj.app.service.vo.fund.FastInFastOutRequest;
import com.zqykj.common.vo.PageRequest;
import com.zqykj.domain.bank.BankTransactionRecord;
import com.zqykj.parameters.query.CombinationQueryParams;
import com.zqykj.parameters.query.QueryOperator;
import com.zqykj.parameters.query.QuerySpecialParams;
import org.springframework.lang.Nullable;

import java.util.Date;
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
     * 构建查询 入账的调单卡号集合d参数
     */
    QuerySpecialParams buildCreditsAdjustCards(String caseId, List<String> adjustCards, int singleQuota);

    /**
     * <h2> 查询出  入账/出账记录 (不在给定的这一组卡号之内的) - 快进快出 </h2>
     */
    QuerySpecialParams buildCreditAndPayoutRecordsNoSuchCards(FastInFastOutRequest request, int size,
                                                              boolean isCredits, String... includeFields);

    /**
     * <h2> 构建 本方查询卡号、对方卡号之间的进账/出账记录 </h2>
     */
    QuerySpecialParams buildCreditAndPayOutViaLocalAndOpposite(String caseId, List<String> queryCards, List<String> oppositeCards,
                                                               int singleQuota, boolean isCredits, @Nullable String... includeFields);

    /**
     * <h2> 快进快出(查询卡号为给定的调单卡号、对方卡号是去除掉这些调单卡号作为主要查询条件) </h2>
     *
     * @param request  快进快出请求
     * @param loanFlag 借贷标志
     */
    QuerySpecialParams queryAsAdjustOppositeNoSuchAdjust(FastInFastOutRequest request, String loanFlag);

    /**
     * <h2> 获取快进快出 进账或者出账交易记录(通过一些组合查询条件) </h2>
     * <p>
     * 限定条件为本方查询卡号
     *
     * @param caseId         案件Id
     * @param singleQuota    单笔限额
     * @param queryCards     查询卡号
     * @param isQueryCredits 查询的借贷标志是出还是进
     * @param tradeDate      交易日期
     * @param operator       日期查询的范围条件
     * @param includeFields  查询返回包含的字段
     */
    QuerySpecialParams getFastInOutTradeRecordsByCondition(String caseId, int singleQuota, List<String> queryCards,
                                                           boolean isQueryCredits, Date tradeDate, QueryOperator operator,
                                                           @Nullable String... includeFields);

    /**
     * <h2> 获取快进快出 进账或者出账交易记录(通过一些组合查询条件) </h2>
     * <p>
     * 限定条件为本方查询卡号 和 对方卡号
     *
     * @param caseId         案件Id
     * @param singleQuota    单笔限额
     * @param queryCards     查询卡号
     * @param oppositeCards  对方卡号
     * @param isQueryCredits 查询的借贷标志是出还是进
     * @param tradeDate      交易日期
     * @param operator       日期查询的范围条件
     * @param includeFields  查询返回包含的字段
     */
    QuerySpecialParams getFastInOutTradeRecordsByLocalOpposite(String caseId, int singleQuota, List<String> queryCards, List<String> oppositeCards,
                                                               boolean isQueryCredits, Date tradeDate, QueryOperator operator, String... includeFields);


    /**
     * 新
     */

    /**
     * <h2> 通过卡号获取进出记录 </h2>
     * <p>
     * 其中cards 卡号作为查询卡号
     */
    QuerySpecialParams getInoutRecordsViaAdjustCards(List<String> cards, String caseId, boolean isIn);

    /**
     * <h2> 获取快进快出结果记录 </h2>
     */
    QuerySpecialParams getFastInFastOutRecord(FastInFastOutRequest request);
}
