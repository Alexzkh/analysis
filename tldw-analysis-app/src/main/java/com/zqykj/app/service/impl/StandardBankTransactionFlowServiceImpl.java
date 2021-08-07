package com.zqykj.app.service.impl;

import com.zqykj.app.service.IStandardBankTransactionFlowService;
import com.zqykj.app.service.config.ElasticsearchProperties;
import com.zqykj.domain.bank.StandardBankTransactionFlow;
import com.zqykj.tldw.aggregate.searching.BaseOperations;
import com.zqykj.tldw.aggregate.searching.ElasticsearchTemplateOperations;
import com.zqykj.tldw.aggregate.searching.esclientrhl.config.ElasticsearchOperationClientProperties;
import com.zqykj.tldw.aggregate.searching.esclientrhl.config.GenerateClientManager;
import com.zqykj.tldw.aggregate.searching.esclientrhl.repository.ElasticsearchTemplate;
import com.zqykj.tldw.aggregate.searching.impl.EsOperationsTempleate;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description: TODO
 * @Author zhangkehou
 * @Date 2021/8/4
 */
@Slf4j
@Service
public class StandardBankTransactionFlowServiceImpl implements IStandardBankTransactionFlowService {

    private ElasticsearchTemplateOperations<StandardBankTransactionFlow,String> elasticsearchTemplate;


    @Autowired
    ElasticsearchProperties elasticsearchProperties;



    @PostConstruct
    public void init(){
        ElasticsearchOperationClientProperties elasticsearchOperationClientProperties = ElasticsearchOperationClientProperties.builder()
                .connectionRequestTimeoutMillis(elasticsearchProperties.getConnectionRequestTimeoutMillis())
                .connectTimeoutMillis(elasticsearchProperties.getConnectTimeoutMillis())
                .host(elasticsearchProperties.getHost())
                .username(elasticsearchProperties.getUsername())
                .maxConnectPerRoute(elasticsearchProperties.getMaxConnectPerRoute())
                .password(elasticsearchProperties.getPassword())
                .socketTimeoutMillis(elasticsearchProperties.getSocketTimeoutMillis())
                .maxConnectTotal(elasticsearchProperties.getMaxConnectTotal())
                .build();

        GenerateClientManager generateClientManager = new GenerateClientManager(elasticsearchOperationClientProperties);
        elasticsearchTemplate = generateClientManager.getTemplete(BaseOperations.DatasoureType.Elasticsearch);


    }

    public List<StandardBankTransactionFlow> standardBankTransactionFlowList() {
        List<StandardBankTransactionFlow> standardBankTransactionFlows = new ArrayList<StandardBankTransactionFlow>();
        try {
            standardBankTransactionFlows  = elasticsearchTemplate.search(new MatchAllQueryBuilder(),StandardBankTransactionFlow.class);
//            standardBankTransactionFlows.stream().limit(10).collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }


        return standardBankTransactionFlows;
    }
}
