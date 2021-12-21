package com.zqykj.app.service.factory.requestparam;

import com.zqykj.common.enums.FundsResultType;
import com.zqykj.parameters.aggregate.AggregationParams;

/**
 * @Description: 资金来源去向聚合操作工厂
 * @Author zhangkehou
 * @Date 2021/11/22
 */
public interface FundSourceAndDestinationAggFactory {

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
