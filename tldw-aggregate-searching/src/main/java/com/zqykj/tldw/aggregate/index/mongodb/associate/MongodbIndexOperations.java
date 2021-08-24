/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.index.mongodb.associate;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.zqykj.tldw.aggregate.properties.MongoDBOperationClientProperties;
import com.zqykj.tldw.aggregate.index.convert.IndexConverters;
import com.zqykj.tldw.aggregate.index.mongodb.SimpleMongoPersistentEntity;
import com.zqykj.tldw.aggregate.index.operation.AbstractDefaultIndexOperations;
import com.zqykj.tldw.aggregate.searching.mongoclientrhl.MongoRestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * <h2> Mongodb Index Operations</h2>
 */
@Slf4j
public class MongodbIndexOperations extends AbstractDefaultIndexOperations
        implements MongodbIndexOperate {

    private final MongoRestTemplate mongoRestTemplate;
    // 该properties 需要替换成专用的mongodb properties
    private final MongoDBOperationClientProperties properties;
    private final IndexResolver indexResolver;
    protected final Class<?> boundClass;
    @Nullable
    protected final String boundCollection;


    public MongodbIndexOperations(MongoRestTemplate mongoRestTemplate,
                                  @NonNull Class<?> boundClass) {
        this.mongoRestTemplate = mongoRestTemplate;
        this.properties = mongoRestTemplate.getMongoDBOperationClientProperties();
        this.indexResolver = IndexResolver.create(mongoRestTemplate.getMappingContext());
        this.boundClass = boundClass;
        this.boundCollection = mongoRestTemplate.getIndexCoordinatesFor(boundClass);
    }

    @Override
    public boolean createIndex() {
        // 解析原类,构建索引定义
        SimpleMongoPersistentEntity<?> mongoPersistentEntity = getRequiredPersistentEntity(boundClass);
        for (IndexDefinition indexDefinition : indexResolver.resolveIndexFor(mongoPersistentEntity)) {

            MongodbPersistentEntityIndexResolver.IndexDefinitionHolder indexToCreate = indexDefinition instanceof MongodbPersistentEntityIndexResolver.IndexDefinitionHolder
                    ? (MongodbPersistentEntityIndexResolver.IndexDefinitionHolder) indexDefinition
                    : new MongodbPersistentEntityIndexResolver.IndexDefinitionHolder("", indexDefinition, boundCollection);
            String indexCreateName = doCreateIndex(indexToCreate, mongoPersistentEntity);
            if (StringUtils.isBlank(indexCreateName)) {
                log.warn("index create fail, index definition collection = {} , index keys = {}", indexToCreate.getCollection(),
                        indexToCreate.getIndexKeys());
            }
        }
        return true;
    }

    private String doCreateIndex(IndexDefinition indexDefinition, SimpleMongoPersistentEntity<?> entity) {
        return execute(entity, collection -> {
            IndexOptions indexOptions = IndexConverters.indexDefinitionToIndexOptionsConverter().convert(indexDefinition);
            if (null != indexOptions) {
                return collection.createIndex(indexDefinition.getIndexKeys(), indexOptions);
            }
            return collection.createIndex(indexDefinition.getIndexKeys());
        });
    }

    public <T> T execute(SimpleMongoPersistentEntity<?> entity, CollectionCallback<T> callback) {
        Assert.notNull(entity.getCollection(), "CollectionName must not be null!");
        Assert.notNull(callback, "CollectionCallback must not be null!");
        Assert.notNull(properties.getDatabase(), "databaseName must not be null!");
        try {
            MongoDatabase db = mongoRestTemplate.execute(client -> client.getDatabase(properties.getDatabase()));
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

    /**
     * <h2> 根据指定Class 获取对应的ElasticsearchPersistentEntity </h2>
     */
    SimpleMongoPersistentEntity<?> getRequiredPersistentEntity(Class<?> clazz) {
        return mongoRestTemplate.getMappingContext().getRequiredPersistentEntity(clazz);
    }

}

