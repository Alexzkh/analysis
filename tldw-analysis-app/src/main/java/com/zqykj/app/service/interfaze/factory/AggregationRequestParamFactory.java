/**
 * @作者 Mcj
 */
package com.zqykj.app.service.interfaze.factory;

import com.zqykj.parameters.aggregate.AggregationParams;

/**
 * <h1> 公共聚合请求参数构建工厂 </h1>
 */
public interface AggregationRequestParamFactory {

    /**
     * 构建获取交易统计分析总数的聚合参数.
     *
     * @param request: 交易统计查询请求体
     * @return: com.zqykj.parameters.aggregate.AggregationParams
     **/
    <T> AggregationParams buildTradeStatisticsAnalysisTotalAgg(T request);

    /**
     * <h2> 构建交易统计分析结果查询参数(用户明确给定一组调单卡号集合) 、全部查询不适用此方法 </h2>
     */
    <T> AggregationParams buildTradeStatisticsAnalysisByMainCards(T request);

    /**
     * 构建获取交易统计分析根据时间类型获取结果的聚合参数.
     *
     * @param request: 交易统计按时间类型统计请求体
     * @return: com.zqykj.parameters.aggregate.AggregationParams
     **/
    <T> AggregationParams buildTradeStatisticsAnalysisFundByTimeType(T request);

    /**
     * 构建获取资产分析的聚合参数.
     *
     * @param request: 构建资产趋势分析查询请求体.
     * @return: com.zqykj.parameters.aggregate.AggregationParams
     **/
    <T> AggregationParams createAssetTrendsAnalysisQueryAgg(T request);

    /**
     * 构建获取人员地域分析的聚合参数.
     *
     * @param request: 人员地域业务请求体.
     * @return: com.zqykj.parameters.aggregate.AggregationParams
     **/
    <T> AggregationParams createPeopleAreaQueryAgg(T request);

    /**
     * 构建资金来源去向中来源的topN
     *
     * @param request: 资金来源去向查询请求体
     * @return: com.zqykj.parameters.aggregate.AggregationParams
     **/
    <T> AggregationParams buildFundsSourceTopNAgg(T request);

    /**
     * <h2>  构建交易汇聚分析结果聚合请求(基于选中一组调单卡号集合为查询条件) </h2>
     */
    <T> AggregationParams buildTradeConvergenceAnalysisResultMainCardsAgg(T request);

    /**
     * <h2> 获取交易汇聚分析结果查询总数据量 </h2>
     */
    <T> AggregationParams buildTradeConvergenceAnalysisResultTotalAgg(T request);

    /**
     * <h2> 批量获取调单卡号集合 </h2>
     * <p>
     * 基于表 {@link com.zqykj.domain.bank.BankTransactionFlow} 查询
     *
     * @param from 偏移量
     * @param size 条数
     */
    AggregationParams buildGetCardNumsInBatchesAgg(int from, int size);

    /**
     * 构建单卡画像最早交易时间聚合查询参数
     *
     * @param request
     * @param <T>
     * @return
     */
    <T> AggregationParams buildSingleCardPortraitEarliestTimeAgg(T request);

    /**
     * 构建单卡画像最晚交易时间聚合查询参数
     *
     * @param request
     * @param <T>
     * @return
     */
    <T> AggregationParams buildSingleCardPortraitLatestTimeAgg(T request);

    /**
     * 构建单卡画像本方卡号分桶聚合查询参数
     *
     * @param request
     * @param <T>
     * @return
     */
    <T> AggregationParams buildSingleCardPortraitAgg(T request);

}
