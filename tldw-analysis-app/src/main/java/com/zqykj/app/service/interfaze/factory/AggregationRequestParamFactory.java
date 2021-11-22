/**
 * @作者 Mcj
 */
package com.zqykj.app.service.interfaze.factory;

import com.zqykj.common.enums.FundsResultType;
import com.zqykj.parameters.aggregate.AggregationParams;

/**
 * <h1> 公共聚合请求参数构建工厂 </h1>
 */
public interface AggregationRequestParamFactory {

    /**
     * 构建获取交易统计分析针对调单卡号在本方的聚合参数.
     *
     * @param request:
     * @return: com.zqykj.parameters.aggregate.AggregationParams
     **/
    <T> AggregationParams buildTradeStatisticsAnalysisQueryCardAgg(T request);

    /**
     * 构建获取交易统计分析针对调单卡号在对方的聚合参数.
     *
     * @param request: 交易统计查询请求体
     * @return: com.zqykj.parameters.aggregate.AggregationParams
     **/
    <T> AggregationParams buildTradeStatisticsAnalysisOppositeCardAgg(T request);

    /**
     * 构建获取交易统计分析总数的聚合参数.
     *
     * @param request: 交易统计查询请求体
     * @return: com.zqykj.parameters.aggregate.AggregationParams
     **/
    <T> AggregationParams buildTradeStatisticsAnalysisTotalAgg(T request);

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
    <T> AggregationParams buildFundsSourceTopNAgg(T request, FundsResultType fundsResultType);

    /**
     * 构建折线图聚合请求参数
     *
     * @param request: 资金来源去向请求体
     * @return: com.zqykj.parameters.aggregate.AggregationParams
     **/
    <T> AggregationParams buildFundsSourceAndDestinationLineChartAgg(T request, FundsResultType type);

    /**
     * 构建列表（个体）数据请求体
     *
     * @param request: 资金来源去向请求体
     * @return: com.zqykj.parameters.aggregate.AggregationParams
     **/
    <T> AggregationParams buildFundsSourceAndDestinationResultListAgg(T request);

    /**
     * 构建列表（个体）数据请求体
     *
     * @param request: 资金来源去向请求体
     * @return: com.zqykj.parameters.aggregate.AggregationParams
     **/
    <T> AggregationParams buildFundsSourceAndDestinationCardResultListAgg(T request);

    /**
     * 构建饼图数据请求体
     *
     * @param request: 资金来源去向请求体
     * @return: com.zqykj.parameters.aggregate.AggregationParams
     **/
    <T> AggregationParams buildFundsSourceAndDestinationPieChartAgg(T request);
}
