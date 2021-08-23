/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.searching.mongoclientrhl;

import com.mongodb.client.MongoClient;
import com.zqykj.tldw.aggregate.index.mongodb.SimpleMongodbMappingContext;
import com.zqykj.tldw.aggregate.properties.MongoDBOperationClientProperties;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.util.Assert;

import java.io.IOException;

/**
 * <h1> Mongodb template </h1>
 */
public class MongoRestTemplate extends AbstractMongoTemplate {

    protected final SimpleMongodbMappingContext mappingContext;
    protected final MongoClient client;
    private final MongoDBOperationClientProperties mongoDBOperationClientProperties;

    public MongoRestTemplate(MongoClient mongoClient, SimpleMongodbMappingContext mappingContext,
                             MongoDBOperationClientProperties mongoDBOperationClientProperties) {
        this.client = mongoClient;
        this.mappingContext = mappingContext;
        this.mongoDBOperationClientProperties = mongoDBOperationClientProperties;
    }

    public final SimpleMongodbMappingContext getMappingContext() {
        return mappingContext;
    }

    public final MongoDBOperationClientProperties getMongoDBOperationClientProperties() {
        return mongoDBOperationClientProperties;
    }

    /**
     * Callback interface to be used with {@link #execute(ClientCallback)} for operating directly on
     * {@link MongoClient}.
     */
    @FunctionalInterface
    public interface ClientCallback<T> {
        T doWithClient(MongoClient client) throws IOException;
    }

    /**
     * Execute a callback with the {@link RestHighLevelClient}
     *
     * @param callback the callback to execute, must not be {@literal null}
     * @param <T>      the type returned from the callback
     * @return the callback result
     */
    public <T> T execute(ClientCallback<T> callback) {

        Assert.notNull(callback, "callback must not be null");

        try {
            return callback.doWithClient(client);
        } catch (IOException | RuntimeException e) {
            throw new RuntimeException(e);
        }
    }
}
