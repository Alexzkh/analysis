/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.searching.esclientrhl;

import com.zqykj.infrastructure.util.Streamable;
import com.zqykj.tldw.aggregate.data.core.elasticsearch.DocumentAdapters;
import com.zqykj.tldw.aggregate.data.query.elasticsearch.core.SearchHitsIterator;
import com.zqykj.tldw.aggregate.data.query.elasticsearch.Query;
import com.zqykj.tldw.aggregate.data.query.elasticsearch.core.*;
import com.zqykj.tldw.aggregate.exception.ElasticsearchExceptionTranslator;
import com.zqykj.tldw.aggregate.index.elasticsearch.SimpleElasticsearchMappingContext;
import com.zqykj.tldw.aggregate.index.elasticsearch.associate.ElasticsearchIndexOperations;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <h1> Elasticsearch rest template </h1>
 */
@Slf4j
public class ElasticsearchRestTemplate extends AbstractElasticsearchTemplate {

    protected final RestHighLevelClient client;
    private final ElasticsearchExceptionTranslator exceptionTranslator;
    protected final SimpleElasticsearchMappingContext mappingContext;

    public ElasticsearchRestTemplate(RestHighLevelClient client,
                                     SimpleElasticsearchMappingContext mappingContext) {
        this.client = client;
        this.mappingContext = mappingContext;
        this.exceptionTranslator = new ElasticsearchExceptionTranslator();
        initialize(mappingContext);
    }

    public final SimpleElasticsearchMappingContext getMappingContext() {
        return mappingContext;
    }

    public <T> T save(T entity, String indexName) {

        Assert.notNull(entity, "entity must not be null");
        Assert.notNull(indexName, "index must not be null");

        IndexRequest indexRequest = getIndexRequest(entity, indexName);
        IndexResponse indexResponse = execute(client -> client.index(indexRequest, RequestOptions.DEFAULT));
        if (indexResponse.getResult() == DocWriteResponse.Result.CREATED) {
            log.info("Index create success !");
        } else if (indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {
            log.info("Index update success !");
        } else {
            log.warn("Index create fail!");
        }
        return entity;
    }

    /**
     * <h2> 批量保存 </h2>
     */
    public <T> Iterable<T> save(Iterable<T> entities, String indexName) {

        Assert.notNull(entities, "entities must not be null");
        Assert.notNull(indexName, "index must not be null");

        List<IndexRequest> indexRequests = Streamable.of(entities).stream().map(entity -> getIndexRequest(entity, indexName)).collect(Collectors.toList());
        BulkRequest bulkRequest = getBulkRequest(indexRequests, BulkOptions.defaultOptions(), indexName);

        // 检查bulk 操作
        checkForBulkOperationFailure(execute(client -> client.bulk(bulkRequest, RequestOptions.DEFAULT)));

        // 返回数据
        return Streamable.of(entities).stream().collect(Collectors.toList());
    }

    /**
     * <h2> 根据Id 与 路由获取单条文档数据 </h2>
     */
    @Nullable
    public <T> T get(String id, Class<T> type, String indexName, @NonNull String routing) {
        GetRequest request = new GetRequest(indexName, id).routing(routing);
        GetResponse response = execute(client -> client.get(request, RequestOptions.DEFAULT));
        DocumentCallback<T> callback = new ReadDocumentCallback<>(elasticsearchConverter, type);
        return callback.doWith(DocumentAdapters.from(response));
    }


    public <T> SearchHit<T> searchOne(Query query, Class<T> clazz, String index) {
        List<SearchHit<T>> content = search(query, clazz, index).getSearchHits();
        return content.isEmpty() ? null : content.get(0);
    }

    public <T> SearchHits<T> search(Query query, Class<T> type, String index) {
        SearchRequest searchRequest = createSearchRequest(query, type, index);
        SearchResponse response = execute(client -> client.search(searchRequest, RequestOptions.DEFAULT));
        SearchDocumentResponseCallback<SearchHits<T>> searchDocumentResponseCallback =
                new ReadSearchDocumentResponseCallback<>(type);
        return searchDocumentResponseCallback.doWith(SearchDocumentResponse.from(response));
    }

    /**
     * <h2> 滚动查询 begin</h2>
     */
    public <T> SearchHits<T> searchScrollStart(long scrollTimeInMillis, Query query, Class<T> clazz,
                                               String index) {

        Assert.notNull(query.getPageable(), "pageable of query must not be null.");

        SearchRequest searchRequest = createSearchRequest(query, clazz, index);
        searchRequest.scroll(TimeValue.timeValueMillis(scrollTimeInMillis));

        SearchResponse response = execute(client -> client.search(searchRequest, RequestOptions.DEFAULT));

        SearchDocumentResponseCallback<SearchHits<T>> callback = new ReadSearchDocumentResponseCallback<>(clazz);
        return callback.doWith(SearchDocumentResponse.from(response));
    }

    /**
     * <h2> 滚动查询 continue</h2>
     */
    public <T> SearchHits<T> searchScrollContinue(@Nullable String scrollId, long scrollTimeInMillis,
                                                  Class<T> clazz, String index) {

        SearchScrollRequest request = new SearchScrollRequest(scrollId);
        request.scroll(TimeValue.timeValueMillis(scrollTimeInMillis));

        SearchResponse response = execute(client -> client.scroll(request, RequestOptions.DEFAULT));

        SearchDocumentResponseCallback<SearchHits<T>> callback = //
                new ReadSearchDocumentResponseCallback<>(clazz);
        return callback.doWith(SearchDocumentResponse.from(response));
    }

    public <T> SearchHitsIterator<T> searchForStream(Query query, Class<T> clazz, String index) {

        long scrollTimeInMillis = TimeValue.timeValueMinutes(1).millis();

        // noinspection ConstantConditions
        int maxCount = query.isLimiting() ? query.getMaxResults() : 0;

        return EsStreamQueries.streamResults(
                maxCount,
                searchScrollStart(scrollTimeInMillis, query, clazz, index),
                scrollId -> searchScrollContinue(scrollId, scrollTimeInMillis, clazz, index),
                this::searchScrollClear);
    }

    public void searchScrollClear(List<String> scrollIds) {
        try {
            ClearScrollRequest request = new ClearScrollRequest();
            request.scrollIds(scrollIds);
            execute(client -> client.clearScroll(request, RequestOptions.DEFAULT));
        } catch (Exception e) {
            log.warn("Could not clear scroll: {}", e.getMessage());
        }
    }


    /**
     * <h2> 获取一个Elasticsearch index operation </h2>
     */
    public ElasticsearchIndexOperations indexOps(Class<?> clazz) {

        Assert.notNull(clazz, "clazz must not be null");

        return new ElasticsearchIndexOperations(this, clazz);
    }

    /**
     * Callback interface to be used with {@link #execute(ClientCallback)} for operating directly on
     * {@link RestHighLevelClient}.
     */
    @FunctionalInterface
    public interface ClientCallback<T> {
        T doWithClient(RestHighLevelClient client) throws IOException;
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
            throw translateException(e);
        }
    }


    private RuntimeException translateException(Exception exception) {

        RuntimeException runtimeException = exception instanceof RuntimeException ? (RuntimeException) exception
                : new RuntimeException(exception.getMessage(), exception);
        RuntimeException potentiallyTranslatedException = exceptionTranslator
                .translateExceptionIfPossible(runtimeException);

        return potentiallyTranslatedException != null ? potentiallyTranslatedException : runtimeException;
    }
}
