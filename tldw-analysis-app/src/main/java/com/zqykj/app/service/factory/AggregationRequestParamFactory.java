/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory;

import com.zqykj.parameters.FieldSort;
import com.zqykj.parameters.Pagination;
import com.zqykj.parameters.aggregate.AggregationParams;
import org.springframework.lang.Nullable;

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
    <T> AggregationParams buildTradeStatisticsAnalysisByMainCards(T request, int from, int size);

    /**
     * <h2> 构建交易统计分析聚合展示字段聚合 </h2>
     */
    AggregationParams buildTradeStatisticalAnalysisHitsAgg(int groupSize);

    /**
     * <h2> 构建交易统计分析结果查询参数(用户明确给定一组调单卡号集合、只返回查询卡号) </h2>
     */
    <T> AggregationParams buildTradeStatisticalQueryCardsAgg(T request, int from, int size);

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
     * <h2>  构建交易汇聚分析结果聚合请求(基于选中一组调单卡号集合为查询条件) </h2>
     */
    <T> AggregationParams buildTradeConvergenceAnalysisResultMainCardsAgg(T request, int from, int size);

    /**
     * <h2> 构建交易汇聚分析结果聚合请求(基于选中一组合并卡号集合为查询条件)  </h2>
     */
    <T> AggregationParams buildTradeConvergenceQueryAndMergeCardsAgg(T request, int from, int size);

    /**
     * <h2> 构建交易汇聚分析聚合展示字段聚合 </h2>
     */
    AggregationParams buildTradeConvergenceAnalysisHitsAgg(int groupSize);

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
     * <h2> 按指定字段groupBy且分页操作 </h2>
     *
     * @param field      分组字段
     * @param groupSize  group by 结果的size eg. 例如实际有20000个,如果不设置默认只返回10个,后续一些sum操作,都是基于这个size操作的
     * @param pagination 分页参数: from: 起始位置 (从0开始) 、 size: 返回结果条件
     */
    AggregationParams groupByField(String field, int groupSize, @Nullable Pagination pagination);

    /**
     * <h2> 聚合展示字段并且按某个字段排序 </h2>
     */
    AggregationParams showFields(@Nullable FieldSort sort, String... fields);


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

    /**
     * <h2> 构建选择个体聚合 </h2>
     */
    <T> AggregationParams buildAdjustIndividualAgg(T request);

    /**
     * <h2> 构建调单卡号聚合 </h2>
     */
    <T> AggregationParams buildAdjustCardsAgg(T request);

    /**
     * <h2> 构建去重请求 根据去重字段 </h2>
     *
     * @param distinctField 去重字段
     */
    AggregationParams buildDistinctViaField(String distinctField);

    /**
     * <h2> 构建查询 入账的调单卡号集合聚合参数 </h2>
     */
    AggregationParams buildCreditsAdjustCardsAgg(int initGroupSize, int from, int size);

    /**
     * <h2> 构建查询 入账的调单卡号集合总量聚合参数 </h2>
     */
    AggregationParams buildCreditsAdjustCardsTotalAgg();

    /**
     * 构建个体画像-基本信息和统计聚合查询参数
     *
     * @param request
     * @param <T>
     * @return
     */
    <T> AggregationParams buildIndividualInfoAndStatisticsAgg(T request);

    /**
     * 构建个体画像-名下卡交易统计聚合查询参数
     *
     * @param request
     * @param <T>
     * @return
     */
    <T> AggregationParams buildIndividualCardTransactionStatisticsAgg(T request);
}
