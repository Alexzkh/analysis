package com.zqykj.app.service.impl;

import com.zqykj.app.service.IStandardBankTransactionFlowService;
import com.zqykj.app.service.dao.ElasticTestDao;
import com.zqykj.domain.bank.StandardBankTransactionFlow;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: TODO
 * @Author zhangkehou
 * @Date 2021/8/4
 */
@Slf4j
//@Component
public class StandardBankTransactionFlowServiceImpl implements IStandardBankTransactionFlowService {

    @Autowired
    private ElasticTestDao elasticTestD;

    //
//
////    @Autowired
////    ElasticsearchProperties elasticsearchProperties;
////
////
////
////    @PostConstruct
////    public void init(){
////        ElasticsearchOperationClientProperties elasticsearchOperationClientProperties = ElasticsearchOperationClientProperties.builder()
////                .connectionRequestTimeoutMillis(elasticsearchProperties.getConnectionRequestTimeoutMillis())
////                .connectTimeoutMillis(elasticsearchProperties.getConnectTimeoutMillis())
////                .host(elasticsearchProperties.getHost())
////                .userName(elasticsearchProperties.getUsername())
////                .maxConnectPerRoute(elasticsearchProperties.getMaxConnectPerRoute())
////                .password(elasticsearchProperties.getPassword())
////                .socketTimeoutMillis(elasticsearchProperties.getSocketTimeoutMillis())
////                .maxConnectTotal(elasticsearchProperties.getMaxConnectTotal())
////                .build();
////
////        GenerateClientManager generateClientManager = new GenerateClientManager(elasticsearchOperationClientProperties);
////        elasticsearchTemplate = (ElasticsearchTemplateOperations) generateClientManager.getTemplete(BaseOperations.DatasoureType.Elasticsearch);
////
////
////    }
//
    public List<StandardBankTransactionFlow> standardBankTransactionFlowList() {
        List<StandardBankTransactionFlow> standardBankTransactionFlows = new ArrayList<StandardBankTransactionFlow>();
        try {
            standardBankTransactionFlows = elasticTestD.search(new MatchAllQueryBuilder(), StandardBankTransactionFlow.class);
//            standardBankTransactionFlows.stream().limit(10).collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }


        return standardBankTransactionFlows;
    }
}
