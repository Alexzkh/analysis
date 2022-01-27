package com.zqykj.app.service.factory.param.agg;

import com.zqykj.parameters.aggregate.AggregationParams;

/**
 * @Description: 资金回路聚合请求工厂类
 * @Author zhangkehou
 * @Date 2022/1/17
 */
public interface FundsLoopAggParamFactory {

    /**
     * 构建获取所有调单账号的聚合请求
     *
     * @return: com.zqykj.parameters.aggregate.AggregationParams
     **/
    AggregationParams buildAccessAllAdjustCardsAgg();
}
