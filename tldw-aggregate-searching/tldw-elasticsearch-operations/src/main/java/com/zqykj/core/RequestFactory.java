/**
 * @作者 Mcj
 */
package com.zqykj.core;

import com.zqykj.core.convert.ElasticsearchConverter;
import com.zqykj.core.document.Document;
import com.zqykj.core.mapping.ElasticsearchPersistentEntity;
import com.zqykj.core.mapping.ElasticsearchPersistentProperty;
import com.zqykj.core.query.FetchSourceFilter;
import com.zqykj.domain.Sort;
import com.zqykj.repository.query.*;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.support.ActiveShardCount;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.*;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

import static org.elasticsearch.index.query.QueryBuilders.wrapperQuery;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * <h1> Elasticsearch Request 构建 </h1>
 */
public class RequestFactory {

    // the default max result window size of Elasticsearch
    static final Integer INDEX_MAX_RESULT_WINDOW = 10_000;

    static final String ID = "id";

    private final ElasticsearchConverter elasticsearchConverter;

    public RequestFactory(ElasticsearchConverter elasticsearchConverter) {
        this.elasticsearchConverter = elasticsearchConverter;
    }


    /**
     * <h2> creates a CreateIndexRequest from the rest-high-level-client library. </h2>
     *
     * @param index    name of the index
     * @param settings optional settings
     * @return request
     */
    public CreateIndexRequest createIndexRequest(String index, @Nullable Document settings, Alias alias) {
        CreateIndexRequest request = new CreateIndexRequest(index);

        if (settings != null && !settings.isEmpty()) {
            request.settings(settings);
        }
        if (alias != null) {
            request.alias(alias);
        }
        return request;
    }

    /**
     * <h2> 删除索引请求 </h2>
     */
    public DeleteIndexRequest deleteIndexRequest(String... indexNames) {

        return new DeleteIndexRequest(indexNames);
    }


    /**
     * <h2> creates a GetIndexRequest from the rest-high-level-client library. </h2>
     *
     * @param indexNames a serial of name of the index
     * @return request
     */
    public GetIndexRequest getIndexRequest(String... indexNames) {
        return new GetIndexRequest(indexNames);
    }

    /**
     * <h2> creates a PutMappingRequest from the rest-high-level-client library.  </h2>
     */
    public PutMappingRequest putMappingRequest(Document mapping, String... indexNames) {

        PutMappingRequest request = new PutMappingRequest(indexNames);
        request.source(mapping);
        return request;
    }

    public MultiGetRequest multiGetRequest(Query query, Class<?> clazz, String index) {

        MultiGetRequest multiGetRequest = new MultiGetRequest();
        getMultiRequestItems(query, clazz, index).forEach(multiGetRequest::add);
        return multiGetRequest;
    }

    private List<MultiGetRequest.Item> getMultiRequestItems(Query searchQuery, Class<?> clazz, String indexName) {

        List<MultiGetRequest.Item> items = new ArrayList<>();

        FetchSourceContext fetchSourceContext = getFetchSourceContext(searchQuery);

        if (!isEmpty(searchQuery.getIds())) {
            for (String id : searchQuery.getIds()) {
                MultiGetRequest.Item item = new MultiGetRequest.Item(indexName, id);

                if (searchQuery.getRoute() != null) {
                    item = item.routing(searchQuery.getRoute());
                }

                if (fetchSourceContext != null) {
                    item.fetchSourceContext(fetchSourceContext);
                }

                items.add(item);
            }
        }
        return items;
    }

    private FetchSourceContext getFetchSourceContext(Query searchQuery) {
        FetchSourceContext fetchSourceContext = null;
        SourceFilter sourceFilter = searchQuery.getSourceFilter();

        if (!isEmpty(searchQuery.getFields())) {
            if (sourceFilter == null) {
                sourceFilter = new FetchSourceFilter(toArray(searchQuery.getFields()), null);
            } else {
                ArrayList<String> arrayList = new ArrayList<>();
                Collections.addAll(arrayList, Objects.requireNonNull(sourceFilter.getIncludes()));
                sourceFilter = new FetchSourceFilter(toArray(arrayList), null);
            }

            fetchSourceContext = new FetchSourceContext(true, sourceFilter.getIncludes(), sourceFilter.getExcludes());
        } else if (sourceFilter != null) {
            fetchSourceContext = new FetchSourceContext(true, sourceFilter.getIncludes(), sourceFilter.getExcludes());
        }
        return fetchSourceContext;
    }

    private VersionType retrieveVersionTypeFromPersistentEntity(Class<?> clazz) {

        VersionType versionType = elasticsearchConverter.getMappingContext().getRequiredPersistentEntity(clazz)
                .getVersionType();

        return versionType != null ? versionType : VersionType.EXTERNAL;
    }

    private String[] toArray(List<String> values) {
        String[] valuesAsArray = new String[values.size()];
        return values.toArray(valuesAsArray);
    }


    /**
     * <h2> creates a RefreshRequest from the rest-high-level-client library.  </h2>
     */
    public RefreshRequest refreshRequest(String... indexNames) {

        return new RefreshRequest(indexNames);
    }

    /**
     * <h2> creates a IndexRequest from the rest-high-level-client library. </h2>
     */
    public IndexRequest indexRequest(IndexQuery query, String indexName) {

        IndexRequest indexRequest;

        if (query.getObject() != null) {
            String id = StringUtils.isEmpty(query.getId()) ? getPersistentEntityId(query.getObject()) : query.getId();
            // If we have a query id and a document id, do not ask ES to generate one.
            if (id != null) {
                indexRequest = new IndexRequest(indexName).id(id);
            } else {
                indexRequest = new IndexRequest(indexName);
            }
            indexRequest.source(elasticsearchConverter.mapObject(query.getObject()).toJson(), Requests.INDEX_CONTENT_TYPE);
        } else if (query.getSource() != null) {
            indexRequest = new IndexRequest(indexName).id(query.getId()).source(query.getSource(),
                    Requests.INDEX_CONTENT_TYPE);
        } else if (!CollectionUtils.isEmpty(query.getSourceMap())) {

            // map 形式
            Map<String, ?> sourceMap = query.getSourceMap();
            elasticsearchConverter.mapMapObject(sourceMap, indexName);
            indexRequest = indexMapRequest(sourceMap, indexName);
        } else {
            throw new ElasticsearchException(
                    "object or source is null, failed to index the document [id: " + query.getId() + ']');
        }

        if (query.getVersion() != null) {
            indexRequest.version(query.getVersion());
            VersionType versionType = retrieveVersionTypeFromPersistentEntity(query.getObject().getClass());
            indexRequest.versionType(versionType);
        }

        if (query.getSeqNo() != null) {
            indexRequest.setIfSeqNo(query.getSeqNo());
        }

        if (query.getPrimaryTerm() != null) {
            indexRequest.setIfPrimaryTerm(query.getPrimaryTerm());
        }

        if (query.getRouting() != null) {
            indexRequest.routing(query.getRouting());
        }

        return indexRequest;
    }

    public IndexRequest indexMapRequest(Map<String, ?> values, String indexName) {

        IndexRequest indexRequest;

        if (!CollectionUtils.isEmpty(values)) {
            String id = StringUtils.isEmpty(values.get(ID)) ? null : String.valueOf(values.get(ID));
            // If we have a query id and a document id, do not ask ES to generate one.
            if (id != null) {
                indexRequest = new IndexRequest(indexName).id(id);
            } else {
                indexRequest = new IndexRequest(indexName);
            }
            indexRequest.source(values);
        } else {
            throw new ElasticsearchException("object or source is null, failed to [index: " + indexName + ']');
        }
        return indexRequest;
    }


    /**
     * <h2> 获取实体Id </h2>
     */
    @Nullable
    private String getPersistentEntityId(Object entity) {

        Object identifier = elasticsearchConverter.getMappingContext() //
                .getRequiredPersistentEntity(entity.getClass()) //
                .getIdentifierAccessor(entity);

        if (identifier != null) {
            return identifier.toString();
        }

        return null;
    }

    /**
     * <h2> 批量请求操作 </h2>
     */
    public BulkRequest bulkRequest(List<?> queries, BulkOptions bulkOptions, String index) {
        BulkRequest bulkRequest = new BulkRequest();

        if (bulkOptions.getTimeout() != null) {
            bulkRequest.timeout(bulkOptions.getTimeout());
        }

        if (bulkOptions.getRefreshPolicy() != null) {
            bulkRequest.setRefreshPolicy(bulkOptions.getRefreshPolicy());
        }

        if (bulkOptions.getWaitForActiveShards() != null) {
            bulkRequest.waitForActiveShards(bulkOptions.getWaitForActiveShards());
        }

        if (bulkOptions.getPipeline() != null) {
            bulkRequest.pipeline(bulkOptions.getPipeline());
        }

        if (bulkOptions.getRoutingId() != null) {
            bulkRequest.routing(bulkOptions.getRoutingId());
        }

        queries.forEach(query -> {

            if (query instanceof IndexQuery) {
                bulkRequest.add(indexRequest((IndexQuery) query, index));
            } else if (query instanceof UpdateQuery) {
                bulkRequest.add(updateRequest((UpdateQuery) query, index));
            }
        });
        return bulkRequest;
    }

    /**
     * <h2> 构建UpdateRequest </h2>
     */
    public UpdateRequest updateRequest(UpdateQuery query, String indexName) {

        UpdateRequest updateRequest = new UpdateRequest(indexName, query.getId());

        if (query.getScript() != null) {
            Map<String, Object> params = query.getParams();

            if (params == null) {
                params = new HashMap<>();
            }
            Script script = new Script(ScriptType.INLINE, query.getLang(), query.getScript(), params);
            updateRequest.script(script);
        }

        if (query.getDocument() != null) {
            updateRequest.doc(query.getDocument());
        }

        if (query.getUpsert() != null) {
            updateRequest.upsert(query.getUpsert());
        }

        if (query.getRouting() != null) {
            updateRequest.routing(query.getRouting());
        }

        if (query.getScriptedUpsert() != null) {
            updateRequest.scriptedUpsert(query.getScriptedUpsert());
        }

        if (query.getDocAsUpsert() != null) {
            updateRequest.docAsUpsert(query.getDocAsUpsert());
        }

        if (query.getFetchSource() != null) {
            updateRequest.fetchSource(query.getFetchSource());
        }

        if (query.getFetchSourceIncludes() != null || query.getFetchSourceExcludes() != null) {
            List<String> includes = query.getFetchSourceIncludes() != null ? query.getFetchSourceIncludes()
                    : Collections.emptyList();
            List<String> excludes = query.getFetchSourceExcludes() != null ? query.getFetchSourceExcludes()
                    : Collections.emptyList();
            updateRequest.fetchSource(includes.toArray(new String[0]), excludes.toArray(new String[0]));
        }

        if (query.getIfSeqNo() != null) {
            updateRequest.setIfSeqNo(query.getIfSeqNo());
        }

        if (query.getIfPrimaryTerm() != null) {
            updateRequest.setIfPrimaryTerm(query.getIfPrimaryTerm());
        }

        if (query.getRefresh() != null) {
            updateRequest.setRefreshPolicy(query.getRefresh().name().toLowerCase());
        }

        if (query.getRetryOnConflict() != null) {
            updateRequest.retryOnConflict(query.getRetryOnConflict());
        }

        if (query.getTimeout() != null) {
            updateRequest.timeout(query.getTimeout());
        }

        if (query.getWaitForActiveShards() != null) {
            updateRequest.waitForActiveShards(ActiveShardCount.parseString(query.getWaitForActiveShards()));
        }

        return updateRequest;
    }

    public DeleteRequest deleteRequest(String id, @Nullable String routing, String indexName) {
        DeleteRequest deleteRequest = new DeleteRequest(indexName, id);

        if (routing != null) {
            deleteRequest.routing(routing);
        }

        return deleteRequest;
    }

    public DeleteByQueryRequest deleteByQueryRequest(Query query, Class<?> clazz, String index) {
        SearchRequest searchRequest = searchRequest(query, clazz, index);
        DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(index) //
                .setQuery(searchRequest.source().query()) //
                .setAbortOnVersionConflict(false) //
                .setRefresh(true);

        if (query.isLimiting()) {
            // noinspection ConstantConditions
            deleteByQueryRequest.setBatchSize(query.getMaxResults());
        }

        if (query.hasScrollTime()) {
            // noinspection ConstantConditions
            deleteByQueryRequest.setScroll(TimeValue.timeValueMillis(query.getScrollTime().toMillis()));
        }

        if (query.getRoute() != null) {
            deleteByQueryRequest.setRouting(query.getRoute());
        }

        return deleteByQueryRequest;
    }

    public SearchRequest searchRequest(Query query, @Nullable Class<?> clazz, String index) {

        SearchRequest searchRequest = prepareSearchRequest(query, clazz, index);
        QueryBuilder elasticsearchQuery = getQuery(query);
        QueryBuilder elasticsearchFilter = getFilter(query);

        searchRequest.source().query(elasticsearchQuery);

        if (elasticsearchFilter != null) {
            searchRequest.source().postFilter(elasticsearchFilter);
        }
        return searchRequest;

    }

    @Nullable
    private QueryBuilder getQuery(Query query) {
        QueryBuilder elasticsearchQuery;

        if (query instanceof NativeSearchQuery) {
            NativeSearchQuery searchQuery = (NativeSearchQuery) query;
            elasticsearchQuery = searchQuery.getQuery();
        } else if (query instanceof StringQuery) {
            StringQuery stringQuery = (StringQuery) query;
            elasticsearchQuery = wrapperQuery(stringQuery.getSource());
        } else {
            throw new IllegalArgumentException("unhandled Query implementation " + query.getClass().getName());
        }

        return elasticsearchQuery;
    }

    @Nullable
    private QueryBuilder getFilter(Query query) {
        QueryBuilder elasticsearchFilter;

        if (query instanceof NativeSearchQuery) {
            NativeSearchQuery searchQuery = (NativeSearchQuery) query;
            elasticsearchFilter = searchQuery.getFilter();
        } else if (query instanceof StringQuery) {
            elasticsearchFilter = null;
        } else {
            throw new IllegalArgumentException("unhandled Query implementation " + query.getClass().getName());
        }

        return elasticsearchFilter;
    }

    private SearchRequest prepareSearchRequest(Query query, @Nullable Class<?> clazz, String... indexNames) {

        Assert.notNull(indexNames, "No index defined for Query");
        Assert.notEmpty(indexNames, "No index defined for Query");

        SearchRequest request = new SearchRequest(indexNames);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.version(true);
        sourceBuilder.trackScores(query.getTrackScores());

        if (query.getSourceFilter() != null) {
            SourceFilter sourceFilter = query.getSourceFilter();
            sourceBuilder.fetchSource(sourceFilter.getIncludes(), sourceFilter.getExcludes());
        }

        if (query.getPageable().isPaged()) {
            sourceBuilder.from((int) query.getPageable().getOffset());
            sourceBuilder.size(query.getPageable().getPageSize());
        } else {
            sourceBuilder.from(0);
            sourceBuilder.size(INDEX_MAX_RESULT_WINDOW);
        }

        if (!query.getFields().isEmpty()) {
            sourceBuilder.fetchSource(query.getFields().toArray(new String[0]), null);
        }

        if (query.getIndicesOptions() != null) {
            request.indicesOptions(query.getIndicesOptions());
        }

        if (query.isLimiting()) {
            // noinspection ConstantConditions
            sourceBuilder.size(query.getMaxResults());
        }

        if (query.getMinScore() > 0) {
            sourceBuilder.minScore(query.getMinScore());
        }

        if (query.getPreference() != null) {
            request.preference(query.getPreference());
        }

        request.searchType(query.getSearchType());

        prepareSort(query, sourceBuilder, getPersistentEntity(clazz));

        HighlightBuilder highlightBuilder = highlightBuilder(query);

        if (highlightBuilder != null) {
            sourceBuilder.highlighter(highlightBuilder);
        }

        if (query instanceof NativeSearchQuery) {
            prepareNativeSearch((NativeSearchQuery) query, sourceBuilder);

        }

        if (query.getTrackTotalHits() != null) {
            sourceBuilder.trackTotalHits(query.getTrackTotalHits());
        } else if (query.getTrackTotalHitsUpTo() != null) {
            sourceBuilder.trackTotalHitsUpTo(query.getTrackTotalHitsUpTo());
        }

        if (StringUtils.hasLength(query.getRoute())) {
            request.routing(query.getRoute());
        }

        request.source(sourceBuilder);
        return request;
    }

    @SuppressWarnings("rawtypes")
    private void prepareSort(Query query, SearchSourceBuilder sourceBuilder,
                             @Nullable ElasticsearchPersistentEntity<?> entity) {

        if (query.getSort() != null) {
            query.getSort().forEach(order -> sourceBuilder.sort(getSortBuilder(order, entity)));
        }

        if (query instanceof NativeSearchQuery) {
            NativeSearchQuery nativeSearchQuery = (NativeSearchQuery) query;
            List<SortBuilder<?>> sorts = nativeSearchQuery.getElasticsearchSorts();
            if (sorts != null) {
                sorts.forEach(sourceBuilder::sort);
            }
        }
    }

    private SortBuilder<?> getSortBuilder(Sort.Order order, @Nullable ElasticsearchPersistentEntity<?> entity) {
        SortOrder sortOrder = order.getDirection().isDescending() ? SortOrder.DESC : SortOrder.ASC;

        if (ScoreSortBuilder.NAME.equals(order.getProperty())) {
            return SortBuilders //
                    .scoreSort() //
                    .order(sortOrder);
        } else {
            ElasticsearchPersistentProperty property = (entity != null) //
                    ? entity.getPersistentProperty(order.getProperty()) //
                    : null;
            String fieldName = property != null ? property.getFieldName() : order.getProperty();

            // TODO GeoDistanceSortBuilder 暂不支持
            return SortBuilders //
                    .fieldSort(fieldName) //
                    .order(sortOrder);
        }
    }

    @Nullable
    public HighlightBuilder highlightBuilder(Query query) {
        HighlightBuilder highlightBuilder = query.getHighlightQuery().map(HighlightQuery::getHighlightBuilder).orElse(null);

        if (highlightBuilder == null) {

            if (query instanceof NativeSearchQuery) {
                NativeSearchQuery searchQuery = (NativeSearchQuery) query;

                if (searchQuery.getHighlightFields() != null || searchQuery.getHighlightBuilder() != null) {
                    highlightBuilder = searchQuery.getHighlightBuilder();

                    if (highlightBuilder == null) {
                        highlightBuilder = new HighlightBuilder();
                    }

                    if (searchQuery.getHighlightFields() != null) {
                        for (HighlightBuilder.Field highlightField : searchQuery.getHighlightFields()) {
                            highlightBuilder.field(highlightField);
                        }
                    }
                }
            }
        }
        return highlightBuilder;
    }

    private void prepareNativeSearch(NativeSearchQuery query, SearchSourceBuilder sourceBuilder) {

        if (!query.getScriptFields().isEmpty()) {
            for (ScriptField scriptedField : query.getScriptFields()) {
                sourceBuilder.scriptField(scriptedField.fieldName(), scriptedField.script());
            }
        }

        if (query.getCollapseBuilder() != null) {
            sourceBuilder.collapse(query.getCollapseBuilder());
        }

        if (!isEmpty(query.getIndicesBoost())) {
            for (IndexBoost indexBoost : query.getIndicesBoost()) {
                sourceBuilder.indexBoost(indexBoost.getIndexName(), indexBoost.getBoost());
            }
        }

        if (!isEmpty(query.getAggregations())) {
            for (AbstractAggregationBuilder<?> aggregationBuilder : query.getAggregations()) {
                sourceBuilder.aggregation(aggregationBuilder);
            }
        }

    }

    @Nullable
    private ElasticsearchPersistentEntity<?> getPersistentEntity(@Nullable Class<?> clazz) {
        return clazz != null ? elasticsearchConverter.getMappingContext().getPersistentEntity(clazz) : null;
    }
}
