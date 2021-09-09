/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.config;

import com.mongodb.client.MongoClient;
import com.zqykj.annotations.Document;
import com.zqykj.tldw.aggregate.properties.ElasticsearchOperationClientProperties;
import com.zqykj.tldw.aggregate.properties.MongoDBOperationClientProperties;
import com.zqykj.tldw.aggregate.index.domain.EntityScanner;
import com.zqykj.tldw.aggregate.index.elasticsearch.SimpleElasticsearchMappingContext;
import com.zqykj.tldw.aggregate.index.model.SimpleTypeHolder;
import com.zqykj.tldw.aggregate.index.mongodb.SimpleMongodbMappingContext;
import com.zqykj.tldw.aggregate.searching.esclientrhl.ElasticsearchRestTemplate;
import com.zqykj.tldw.aggregate.searching.mongoclientrhl.MongoRestTemplate;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * <h1> Aggregate Data Mapping Context initialization</h1>
 */
@Configuration
public class AggregateDataConfiguration {

    /**
     * <h2> mongodb context initialization </h2>
     */
    @Bean
    @ConditionalOnBean(MongoDBOperationClientProperties.class)
    @ConditionalOnMissingBean(SimpleMongodbMappingContext.class)
    @ConditionalOnExpression("'${enable.datasource.type}'.equals('mongodb')")
    SimpleMongodbMappingContext simpleMongodbMappingContext(ApplicationContext applicationContext, MongoDBOperationClientProperties properties) throws ClassNotFoundException {
        SimpleMongodbMappingContext context = new SimpleMongodbMappingContext();
        // 是否自动创建索引
        context.setAutoIndexCreation(properties.getAutoIndexCreation());
        context.setInitialEntitySet(new EntityScanner(applicationContext).scan(Document.class));
        context.setSimpleTypeHolder(SimpleTypeHolder.DEFAULT);
        return context;
    }

    @Bean
    @ConditionalOnBean(ElasticsearchOperationClientProperties.class)
    @ConditionalOnMissingBean(SimpleElasticsearchMappingContext.class)
    @ConditionalOnExpression("'${enable.datasource.type}'.equals('elasticsearch')")
    SimpleElasticsearchMappingContext simpleElasticsearchMappingContext(ApplicationContext applicationContext,
                                                                        ElasticsearchOperationClientProperties properties) throws ClassNotFoundException {
        SimpleElasticsearchMappingContext context = new SimpleElasticsearchMappingContext();
        context.setAutoIndexCreation(properties.getAutoIndexCreation());
        context.setInitialEntitySet(new EntityScanner(applicationContext).scan(Document.class));
        context.setSimpleTypeHolder(SimpleTypeHolder.DEFAULT);
        return context;
    }

    @Bean
    @ConditionalOnBean(SimpleMongodbMappingContext.class)
    @ConditionalOnMissingBean(MongoRestTemplate.class)
    public MongoRestTemplate mongoRestTemplate(MongoClient mongoClient,
                                               SimpleMongodbMappingContext simpleMongodbMappingContext,
                                               MongoDBOperationClientProperties mongoDBOperationClientProperties) {

        return new MongoRestTemplate(mongoClient, simpleMongodbMappingContext, mongoDBOperationClientProperties);
    }

    @Bean
    @ConditionalOnBean(SimpleElasticsearchMappingContext.class)
    @ConditionalOnMissingBean(ElasticsearchRestTemplate.class)
    public ElasticsearchRestTemplate elasticsearchRestTemplate(RestHighLevelClient restHighLevelClient,
                                                               SimpleElasticsearchMappingContext simpleElasticsearchMappingContext) {
        
        return new ElasticsearchRestTemplate(restHighLevelClient, simpleElasticsearchMappingContext);
    }
}