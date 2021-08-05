/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.data.query;

import com.mongodb.client.MongoClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class AggregateRepositoryQueryConfiguration {


    @Bean
    @ConditionalOnBean(RestHighLevelClient.class)
    @ConditionalOnExpression("'${enable.datasource.type}'.equals('elasticsearch')")
    public AggregateElasticsearchRepositoryStringQuery elasticsearchRepositoryStringQuery(RestHighLevelClient restHighLevelClient) {
        return new AggregateElasticsearchRepositoryStringQuery(restHighLevelClient, null);
    }


    @Bean
    @ConditionalOnBean(MongoClient.class)
    @ConditionalOnExpression("'${enable.datasource.type}'.equals('mongodb')")
    public AggregateMongoRepositoryStringQuery mongoRepositoryStringQuery(MongoClient mongoClient) {
        return new AggregateMongoRepositoryStringQuery(mongoClient, null);
    }
}
