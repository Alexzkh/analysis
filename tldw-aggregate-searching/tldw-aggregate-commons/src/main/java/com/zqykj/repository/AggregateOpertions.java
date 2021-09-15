package com.zqykj.repository;

import com.zqykj.common.request.AggregateBuilder;

/**
 * @Description: 聚合操作的相关接口
 * @Author zhangkehou
 * @Date 2021/9/15
 */
public interface AggregateOpertions {



    public <T> T aggs(AggregateBuilder aggregateBuilder ,String indexName ,String routing);
}
