/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.index.elasticsearch.associate;

import com.zqykj.tldw.aggregate.index.context.AbstractMappingContext;
import com.zqykj.tldw.aggregate.index.context.AggregateDataSourceMappingContextEvent;
import com.zqykj.tldw.aggregate.index.elasticsearch.SimpleElasticSearchPersistentEntity;
import com.zqykj.tldw.aggregate.index.mapping.BasicPersistentEntity;
import com.zqykj.tldw.aggregate.index.operation.IndexOperations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;

/**
 * <h1> Elasticsearch auto create PersistentEntity Index</h1>
 */
@Slf4j
public class ElasticPersistentEntityIndexCreator
        implements ApplicationListener<AggregateDataSourceMappingContextEvent<?, ?>> {

    public ElasticPersistentEntityIndexCreator(IndexOperations indexOperations, AbstractMappingContext<?, ?> mappingContext) {
        for (BasicPersistentEntity<?, ?> entity : mappingContext.getPersistentEntities()) {
            if (entity instanceof SimpleElasticSearchPersistentEntity<?>) {
                // 处理扫描到的PersistentEntities,自动创建索引
                indexOperations.createIndex(entity);
            }
        }
    }

    /**
     * <h2> 每扫描到一个拥有指定注解的持久化实体都会发布到这里进行相关处理,需要将此类打上@Compontent </h2>
     */
    @Override
    public void onApplicationEvent(AggregateDataSourceMappingContextEvent<?, ?> event) {

        // 这里也可以使用发布订阅的方式,去自动构建索引与mapping
//        if (event.wasEmittedBy(mappingContext)) {
//            // 是否设置了自动创建索引
//            if (mappingContext instanceof SimpleElasticsearchMappingContext) {
//                SimpleElasticsearchMappingContext simpleElasticsearchMappingContext =
//                        (SimpleElasticsearchMappingContext) mappingContext;
//                // 是否指定要自动创建索引
//                if (simpleElasticsearchMappingContext.isAutoIndexCreation()) {
//                    indexOperations.createIndex(event.getPersistentEntity());
//                }
//            }
//        }
    }
}
