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
import com.zqykj.tldw.aggregate.searching.esclientrhl.ElasticsearchRestTemplate;
import com.zqykj.tldw.aggregate.searching.mongoclientrhl.MongoRestTemplate;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
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

//    @Bean
//    @Deprecated
////    @ConditionalOnExpression("'${enable.datasource.type}'.equals('mongodb')")
//    @ConditionalOnBean({SimpleMongodbMappingContext.class})
//    MongodbIndexOperations mongodbIndexOperations(SimpleMongodbMappingContext simpleMongodbMappingContext,
//                                                  MongoClient mongoClient,
//                                                  MongoDBOperationClientProperties properties) {
//        // TODO 迁移到具体实现类中(需要创建Mongodb 数据源顶级接口 与 对应实现类)
////        if (simpleMongodbMappingContext.isAutoIndexCreation()) {
////            new MongodbPersistentEntityIndexCreator(simpleMongodbMappingContext,
////                    IndexResolver.create(simpleMongodbMappingContext),
////                    mongoClient, mongodbIndexOperations, properties);
////        }
//        return new MongodbIndexOperations(mongoClient, simpleMongodbMappingContext, properties);
//    }

//    @Bean
//    @Deprecated
////    @ConditionalOnExpression("'${enable.datasource.type}'.equals('elasticsearch')")
//    @ConditionalOnBean({SimpleElasticsearchMappingContext.class})
//    ElasticsearchIndexOperations elasticsearchIndexOperations(SimpleElasticsearchMappingContext simpleElasticsearchMappingContext,
//                                                              RestHighLevelClient restHighLevelClient) {
    // 迁移到具体数据源的实现类中(保证扫描的索引类 有对应的repository 并且 es配置类中 启动了自动创建索引 且 索引在es中不存在的时候才会创建
//        if (simpleElasticsearchMappingContext.isAutoIndexCreation()) {
//            new ElasticPersistentEntityIndexCreator(elasticsearchIndexOperations, simpleElasticsearchMappingContext);
//        }
//        return new ElasticsearchIndexOperations(restHighLevelClient, simpleElasticsearchMappingContext);
//    }

    @Bean
    @ConditionalOnBean(SimpleMongodbMappingContext.class)
    public MongoRestTemplate mongoRestTemplate(MongoClient mongoClient,
                                               SimpleMongodbMappingContext simpleMongodbMappingContext,
                                               MongoDBOperationClientProperties mongoDBOperationClientProperties) {

        return new MongoRestTemplate(mongoClient, simpleMongodbMappingContext, mongoDBOperationClientProperties);
    }

    @Bean
    @ConditionalOnBean(SimpleElasticsearchMappingContext.class)
    public ElasticsearchRestTemplate elasticsearchRestTemplate(RestHighLevelClient restHighLevelClient,
                                                               SimpleElasticsearchMappingContext simpleElasticsearchMappingContext) {
        return new ElasticsearchRestTemplate(restHighLevelClient, simpleElasticsearchMappingContext);
    }
}