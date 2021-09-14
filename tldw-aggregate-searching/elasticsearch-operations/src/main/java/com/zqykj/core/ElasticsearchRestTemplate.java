/**
 * @作者 Mcj
 */
package com.zqykj.core;

import com.zqykj.core.convert.ElasticsearchConverter;
import com.zqykj.core.document.DocumentAdapters;
import com.zqykj.core.document.SearchDocumentResponse;
import com.zqykj.repository.query.*;
import com.zqykj.support.SearchHitsUtil;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
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

    public ElasticsearchRestTemplate(RestHighLevelClient client) throws Exception {

        Assert.notNull(client, "Client must not be null!");

        this.client = client;
        this.exceptionTranslator = new ElasticsearchExceptionTranslator();

        initialize(createElasticsearchConverter());
    }

    public ElasticsearchRestTemplate(RestHighLevelClient client, ElasticsearchConverter elasticsearchConverter) {

        Assert.notNull(client, "Client must not be null!");

        this.client = client;
        this.exceptionTranslator = new ElasticsearchExceptionTranslator();

        initialize(elasticsearchConverter);
    }

    @Override
    public ElasticsearchIndexOperations indexOps(Class<?> clazz) {

        Assert.notNull(clazz, "clazz must not be null");

        return new ElasticsearchIndexOperations(this, clazz);
    }

    @Override
    public ElasticsearchIndexOperations indexOps(String index) {

        Assert.notNull(index, "index must not be null");

        return new ElasticsearchIndexOperations(this, index);
    }

    @Override
    public List<IndexedObjectInformation> doBulkOperation(List<?> queries, BulkOptions bulkOptions,
                                                          String index) {
        BulkRequest bulkRequest = requestFactory.bulkRequest(queries, bulkOptions, index);
        return checkForBulkOperationFailure(
                execute(client -> client.bulk(bulkRequest, RequestOptions.DEFAULT)));
    }

    /**
     * <h2> 索引操作 </h2>
     */
    @Override
    public String doIndex(IndexQuery query, String index) {

        IndexRequest request = requestFactory.indexRequest(query, index);
        IndexResponse indexResponse = execute(client -> client.index(request, RequestOptions.DEFAULT));

        return indexResponse.getId();
    }

    @Override
    @Nullable
    public <T> T get(String id, Class<T> clazz,String routing) {
        return get(id, clazz, getIndexCoordinatesFor(clazz),routing);
    }

    /**
     * <h2> 根据Id 与 路由获取单条文档数据 </h2>
     */
    @Override
    @Nullable
    public <T> T get(String id, Class<T> clazz, String index, @NonNull String routing) {
        Assert.notNull(routing, "routing param is must be not null!");
        GetRequest request = new GetRequest(index, id).routing(routing);
        GetResponse response = execute(client -> client.get(request, RequestOptions.DEFAULT));

        DocumentCallback<T> callback = new ReadDocumentCallback<>(elasticsearchConverter, clazz, index);
        return callback.doWith(DocumentAdapters.from(response));
    }

    @Override
    public <T> List<T> multiGet(Query query, Class<T> clazz, String index) {

        Assert.notNull(index, "index must not be null");
        Assert.notEmpty(query.getIds(), "No Id define for Query");

        MultiGetRequest request = requestFactory.multiGetRequest(query, clazz, index);
        MultiGetResponse result = execute(client -> client.mget(request, RequestOptions.DEFAULT));

        DocumentCallback<T> callback = new ReadDocumentCallback<>(elasticsearchConverter, clazz, index);
        return DocumentAdapters.from(result).stream().map(callback::doWith).collect(Collectors.toList());
    }

    @Override
    public UpdateResponse update(UpdateQuery query, String index) {
        UpdateRequest request = requestFactory.updateRequest(query, index);
        UpdateResponse.Result result = UpdateResponse.Result
                .valueOf(execute(client -> client.update(request, RequestOptions.DEFAULT)).getResult().name());
        return new UpdateResponse(result);
    }

    @Override
    public String delete(String id, @Nullable String routing, String index) {

        Assert.notNull(id, "id must not be null");
        Assert.notNull(index, "index must not be null");

        DeleteRequest request = requestFactory.deleteRequest(elasticsearchConverter.convertId(id), routing, index);
        return execute(client -> client.delete(request, RequestOptions.DEFAULT).getId());
    }

    @Override
    public void delete(Query query, Class<?> clazz, String index) {
        DeleteByQueryRequest deleteByQueryRequest = requestFactory.deleteByQueryRequest(query, clazz, index);
        execute(client -> client.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT));
    }

    @Override
    public long count(Query query, @Nullable Class<?> clazz, String index) {

        Assert.notNull(query, "query must not be null");
        Assert.notNull(index, "index must not be null");

        final Boolean trackTotalHits = query.getTrackTotalHits();
        query.setTrackTotalHits(true);
        SearchRequest searchRequest = requestFactory.searchRequest(query, clazz, index);
        query.setTrackTotalHits(trackTotalHits);

        searchRequest.source().size(0);

        return SearchHitsUtil.getTotalCount(execute(client -> client.search(searchRequest, RequestOptions.DEFAULT).getHits()));
    }

    @Override
    public <T> SearchHits<T> search(Query query, Class<T> clazz, String index) {
        SearchRequest searchRequest = requestFactory.searchRequest(query, clazz, index);
        SearchResponse response = execute(client -> client.search(searchRequest, RequestOptions.DEFAULT));

        SearchDocumentResponseCallback<SearchHits<T>> callback = new ReadSearchDocumentResponseCallback<>(clazz, index);
        return callback.doWith(SearchDocumentResponse.from(response));
    }

    @Override
    public <T> SearchHitsIterator<T> searchForStream(Query query, Class<T> clazz) {
        return searchForStream(query, clazz, getIndexCoordinatesFor(clazz));
    }

    @Override
    public <T> SearchHitsIterator<T> searchForStream(Query query, Class<T> clazz, String index) {

        long scrollTimeInMillis = TimeValue.timeValueMinutes(1).millis();

        // noinspection ConstantConditions
        int maxCount = query.isLimiting() ? query.getMaxResults() : 0;

        return StreamQueries.streamResults( //
                maxCount, //
                searchScrollStart(scrollTimeInMillis, query, clazz, index), //
                scrollId -> searchScrollContinue(scrollId, scrollTimeInMillis, clazz, index), //
                this::searchScrollClear);
    }

    @Override
    public <T> SearchScrollHits<T> searchScrollStart(long scrollTimeInMillis, Query query, Class<T> clazz,
                                                     String index) {

        Assert.notNull(query.getPageable(), "pageable of query must not be null.");

        SearchRequest searchRequest = requestFactory.searchRequest(query, clazz, index);
        searchRequest.scroll(TimeValue.timeValueMillis(scrollTimeInMillis));

        SearchResponse response = execute(client -> client.search(searchRequest, RequestOptions.DEFAULT));

        SearchDocumentResponseCallback<SearchScrollHits<T>> callback = new ReadSearchScrollDocumentResponseCallback<>(clazz,
                index);
        return callback.doWith(SearchDocumentResponse.from(response));
    }

    @Override
    public <T> SearchScrollHits<T> searchScrollContinue(@Nullable String scrollId, long scrollTimeInMillis,
                                                        Class<T> clazz, String index) {

        SearchScrollRequest request = new SearchScrollRequest(scrollId);
        request.scroll(TimeValue.timeValueMillis(scrollTimeInMillis));

        SearchResponse response = execute(client -> client.scroll(request, RequestOptions.DEFAULT));

        SearchDocumentResponseCallback<SearchScrollHits<T>> callback = //
                new ReadSearchScrollDocumentResponseCallback<>(clazz, index);
        return callback.doWith(SearchDocumentResponse.from(response));
    }

    @Override
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
