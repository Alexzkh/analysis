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
import com.zqykj.tldw.aggregate.index.elasticsearch.associate.ElasticPersistentEntityIndexCreator;
import com.zqykj.tldw.aggregate.index.elasticsearch.associate.ElasticsearchIndexOperations;
import com.zqykj.tldw.aggregate.index.model.SimpleTypeHolder;
import com.zqykj.tldw.aggregate.index.mongodb.SimpleMongodbMappingContext;
import com.zqykj.tldw.aggregate.index.mongodb.associate.IndexResolver;
import com.zqykj.tldw.aggregate.index.mongodb.associate.MongodbIndexOperations;
import com.zqykj.tldw.aggregate.index.mongodb.associate.MongodbPersistentEntityIndexCreator;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * <h1> Aggregate Data Mapping Context initialization</h1>
 */
@Configuration(proxyBeanMethods = false)
public class AggregateDataConfiguration {

    /**
     * <h2> mongodb context initialization </h2>
     */
    @Bean
    @ConditionalOnBean(MongoDBOperationClientProperties.class)
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
//    @ConditionalOnExpression("'${enable.datasource.type}'.equals('mongodb')")
    @ConditionalOnBean({SimpleMongodbMappingContext.class})
    MongodbIndexOperations mongodbIndexOperations(SimpleMongodbMappingContext simpleMongodbMappingContext,
                                                  MongoClient mongoClient,
                                                  MongoDBOperationClientProperties properties) {
        MongodbIndexOperations mongodbIndexOperations = new MongodbIndexOperations(mongoClient, simpleMongodbMappingContext, properties);
        // 若开启自动创建索引
        if (simpleMongodbMappingContext.isAutoIndexCreation()) {
            new MongodbPersistentEntityIndexCreator(simpleMongodbMappingContext,
                    IndexResolver.create(simpleMongodbMappingContext),
                    mongoClient, mongodbIndexOperations, properties);
        }
        return mongodbIndexOperations;
    }

    @Bean
//    @ConditionalOnExpression("'${enable.datasource.type}'.equals('elasticsearch')")
    @ConditionalOnBean({SimpleElasticsearchMappingContext.class})
    ElasticsearchIndexOperations elasticsearchIndexOperations(SimpleElasticsearchMappingContext simpleElasticsearchMappingContext,
                                                              RestHighLevelClient restHighLevelClient) {
        ElasticsearchIndexOperations elasticsearchIndexOperations =
                new ElasticsearchIndexOperations(restHighLevelClient, simpleElasticsearchMappingContext);
        if (simpleElasticsearchMappingContext.isAutoIndexCreation()) {
            new ElasticPersistentEntityIndexCreator(elasticsearchIndexOperations, simpleElasticsearchMappingContext);
        }
        return elasticsearchIndexOperations;
    }
}