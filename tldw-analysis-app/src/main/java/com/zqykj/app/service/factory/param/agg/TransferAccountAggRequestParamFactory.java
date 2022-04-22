package com.zqykj.app.service.factory.param.agg;

import com.zqykj.parameters.aggregate.AggregationParams;

/**
 * @Description: 调单账号聚合请求参数工厂
 * @Author zhangkehou
 * @Date 2021/12/25
 */
public interface TransferAccountAggRequestParamFactory {

    /**
     * 构建调单账号特征分析的聚合请求
     *
     * @param request: 调单账号特征分析聚合请求
     * @return: com.zqykj.parameters.aggregate.AggregationParams
     **/
    <T> AggregationParams buildTransferAccountAgg(T request);

    /**
     * 构建获取所有调单账号的聚合请求
     *
     * @return: com.zqykj.parameters.aggregate.AggregationParams
     **/
    AggregationParams buildAccessAllAdjustCardsAgg();
}
