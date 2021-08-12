/**
 * @author Mcj
 */
package com.zqykj.tldw.aggregate.index.mongodb.associate;

import com.mongodb.client.MongoClient;
import com.zqykj.annotations.Document;
import com.zqykj.tldw.aggregate.properties.MongoDBOperationClientProperties;
import com.zqykj.tldw.aggregate.index.context.AbstractMappingContext;
import com.zqykj.tldw.aggregate.index.context.AggregateDataSourceMappingContextEvent;
import com.zqykj.tldw.aggregate.index.mapping.BasicPersistentEntity;
import com.zqykj.tldw.aggregate.index.mongodb.SimpleMongoPersistentEntity;
import com.zqykj.tldw.aggregate.index.operation.IndexOperations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.util.Assert;

/**
 * <h1> Mongodb auto create PersistentEntity Index</h1>
 */
@Slf4j
public class MongodbPersistentEntityIndexCreator implements
        ApplicationListener<AggregateDataSourceMappingContextEvent<?, ?>> {

    private final IndexOperations indexOperations;

    public MongodbPersistentEntityIndexCreator(AbstractMappingContext<?, ?> mappingContext,
                                               IndexResolver indexResolver, MongoClient mongoClient,
                                               IndexOperations indexOperations,
                                               MongoDBOperationClientProperties properties) {
        Assert.notNull(mappingContext, "MongoMappingContext must not be null!");
        Assert.notNull(indexResolver, "IndexResolver must not be null!");
        Assert.notNull(mongoClient, "MongoClient must not be null!");
        Assert.notNull(indexOperations, "IndexOperations must not be null!");
        Assert.notNull(properties, "Properties must not be null!");
        this.indexOperations = indexOperations;
        for (BasicPersistentEntity<?, ?> entity : mappingContext.getPersistentEntities()) {
            if (entity instanceof SimpleMongoPersistentEntity<?>) {
                // 处理扫描到的PersistentEntities,自动创建索引
                checkForAndCreateIndexes((SimpleMongoPersistentEntity<?>) entity);
            }
        }
    }

    @Override
    public void onApplicationEvent(AggregateDataSourceMappingContextEvent<?, ?> event) {
//        if (!event.wasEmittedBy(mappingContext)) {
//            return;
//        }
//
//        PersistentEntity<?, ?> entity = event.getPersistentEntity();
//
//        // Double check type as Spring infrastructure does not consider nested generics
//        if (entity instanceof SimpleMongoPersistentEntity) {
//
//            checkForAndCreateIndexes((SimpleMongoPersistentEntity<?>) entity);
//        }
    }

    private void checkForAndCreateIndexes(SimpleMongoPersistentEntity<?> entity) {

        if (entity.isAnnotationPresent(Document.class)) {
            createIndex(entity);
        }
    }

    private void createIndex(SimpleMongoPersistentEntity<?> entity) {

        try {
            indexOperations.createIndex(entity);
        } catch (Exception ex) {
            log.error(ex.toString());
            throw ex;
        }
    }
}
