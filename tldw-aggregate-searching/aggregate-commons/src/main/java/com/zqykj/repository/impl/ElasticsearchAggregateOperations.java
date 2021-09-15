package com.zqykj.repository.impl;

import com.zqykj.common.request.AggregateBuilder;
import com.zqykj.repository.AggregateOpertions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Description: TODO
 * @Author zhangkehou
 * @Date 2021/9/15
 */
public class ElasticsearchAggregateOperations implements AggregateOpertions {

    @Autowired
    private RestHighLevelClient restHighLevelClient ;


    @Override
    public <T> T aggs(AggregateBuilder aggregateBuilder, String indexName, String routing) {


        return null;
    }
}
