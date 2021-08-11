/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.index.mongodb.associate;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.zqykj.tldw.aggregate.datasourceconfig.AggregationDataSourceProperties;
import com.zqykj.tldw.aggregate.index.convert.IndexConverters;
import com.zqykj.tldw.aggregate.index.mapping.PersistentEntity;
import com.zqykj.tldw.aggregate.index.mongodb.SimpleMongoPersistentEntity;
import com.zqykj.tldw.aggregate.index.mongodb.SimpleMongodbMappingContext;
import com.zqykj.tldw.aggregate.index.operation.AbstractDefaultIndexOperations;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * <h2> Mongodb Index Operations</h2>
 */
@Slf4j
@SuppressWarnings({"rawtypes"})
public class MongodbIndexOperations extends AbstractDefaultIndexOperations {

    private final MongoClient mongoClient;
    // 该properties 需要替换成专用的mongodb properties
    private final AggregationDataSourceProperties properties;
    private final IndexResolver indexResolver;


    public MongodbIndexOperations(MongoClient mongoClient,
                                  SimpleMongodbMappingContext mappingContext,
                                  AggregationDataSourceProperties properties) {
        this.mongoClient = mongoClient;
        this.properties = properties;
        this.indexResolver = IndexResolver.create(mappingContext);
    }


    @Override
    public boolean createIndex(PersistentEntity<?, ?> entity) {
        if (entity instanceof SimpleMongoPersistentEntity<?>) {
            SimpleMongoPersistentEntity<?> mongoPersistentEntity = (SimpleMongoPersistentEntity<?>) entity;
            String collection = mongoPersistentEntity.getCollection();
            // 解析原类,构建索引定义
            for (IndexDefinition indexDefinition : indexResolver.resolveIndexFor(entity.getTypeInformation())) {

                MongodbPersistentEntityIndexResolver.IndexDefinitionHolder indexToCreate = indexDefinition instanceof MongodbPersistentEntityIndexResolver.IndexDefinitionHolder
                        ? (MongodbPersistentEntityIndexResolver.IndexDefinitionHolder) indexDefinition
                        : new MongodbPersistentEntityIndexResolver.IndexDefinitionHolder("", indexDefinition, collection);
                String indexCreateName = doCreateIndex(indexToCreate, mongoPersistentEntity);
                if (StringUtils.isBlank(indexCreateName)) {
                    log.warn("index create fail, index definition collection = {} , index keys = {}", indexToCreate.getCollection(),
                            indexToCreate.getIndexKeys());
                }
            }
            return true;
        }
        log.warn("PersistentEntity dose not belong SimpleMongoPersistentEntity, can not auto create Index!");
        return false;
    }

    private String doCreateIndex(IndexDefinition indexDefinition, SimpleMongoPersistentEntity entity) {
        return execute(entity, collection -> {
            IndexOptions indexOptions = IndexConverters.indexDefinitionToIndexOptionsConverter().convert(indexDefinition);
            if (null != indexOptions) {
                return collection.createIndex(indexDefinition.getIndexKeys(), indexOptions);
            }
            return collection.createIndex(indexDefinition.getIndexKeys());
        });
    }

    public <T> T execute(SimpleMongoPersistentEntity entity, CollectionCallback<T> callback) {
        Assert.notNull(entity.getCollection(), "CollectionName must not be null!");
        Assert.notNull(callback, "CollectionCallback must not be null!");
        Assert.notNull(properties.getDatabase(), "databaseName must not be null!");
        try {
            MongoDatabase db = mongoClient.getDatabase(properties.getDatabase());
            MongoCollection<Document> collection = db.getCollection(entity.getCollection(), Document.class);
            return callback.doInCollection(collection);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return null;
        }
    }

    /**
     * 用来操作Mongodb Collection
     */
    interface CollectionCallback<T> {

        /**
         * @param collection never {@literal null}.
         * @return can be {@literal null}.
         */
        @Nullable
        T doInCollection(MongoCollection<Document> collection) throws Exception;
    }

}

