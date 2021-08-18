/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.data.query.elasticsearch;

import com.alibaba.fastjson.JSON;
import com.zqykj.domain.page.*;
import com.zqykj.infrastructure.util.CloseableIterator;
import com.zqykj.infrastructure.util.StreamUtils;
import com.zqykj.tldw.aggregate.SearchHitsIterator;
import com.zqykj.annotations.Highlight;
import com.zqykj.tldw.aggregate.data.query.AbstractAggregateRepositoryQuery;
import com.zqykj.tldw.aggregate.data.query.AggregateRepositoryQuery;
import com.zqykj.tldw.aggregate.data.query.elasticsearch.core.*;
import com.zqykj.tldw.aggregate.data.repository.RepositoryInformation;
import com.zqykj.tldw.aggregate.index.elasticsearch.SimpleElasticSearchPersistentEntity;
import com.zqykj.tldw.aggregate.index.elasticsearch.SimpleElasticSearchPersistentProperty;
import com.zqykj.tldw.aggregate.index.elasticsearch.SimpleElasticsearchMappingContext;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.*;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.elasticsearch.index.query.QueryBuilders.wrapperQuery;

/**
 * <h1> 处理@Query 注解的query <h1/>
 */
@Slf4j
public class AggregateElasticsearchRepositoryStringQuery extends AbstractAggregateRepositoryQuery
        implements AggregateRepositoryQuery {

    private final RestHighLevelClient restHighLevelClient;
    private final RepositoryInformation repositoryInformation;
    private final SimpleElasticsearchMappingContext mappingContext;
    private final String query;
    static final Integer INDEX_MAX_RESULT_WINDOW = 10_000;
    protected static final int DEFAULT_STREAM_BATCH_SIZE = 500;

    public AggregateElasticsearchRepositoryStringQuery(RestHighLevelClient restHighLevelClient,
                                                       RepositoryInformation repositoryInformation,
                                                       SimpleElasticsearchMappingContext mappingContext,
                                                       Method method,
                                                       String query) {
        super(method);
        Assert.notNull(restHighLevelClient, "Elasticsearch high rest client cannot be empty!");
        this.repositoryInformation = repositoryInformation;
        this.restHighLevelClient = restHighLevelClient;
        this.mappingContext = mappingContext;
        this.query = query;
    }

    @Override
    public Object execute(Object[] parameters) {

        // 当前domain
        Class<?> domainType = this.repositoryInformation.getDomainType();
        // 获取domainType 的Index 名称
        String indexName = mappingContext.getRequiredPersistentEntity(domainType).getIndexName();
        // 构建查询参数
        ElasticsearchStringQuery stringQuery = createQuery(parameters);
        // 查看此时的method 是否有高亮注解(Highlight)
        if (this.method.isAnnotationPresent(Highlight.class)) {
            Highlight highlight = this.method.getAnnotation(Highlight.class);
            stringQuery.setHighlightQuery(getHighlightQuery(highlight, domainType));
        }
        Object result;
        // 判断是Page 分页查询、stream流查询、collection查询、普通查询方式

        if (isPageQuery()) {

            // 如果method 有分页参数需要设置的话
            stringQuery.setPageable(getPageable(parameters));
            // 使用client 进行查询
            SearchHits<?> searchHits = search(stringQuery, domainType, indexName);
            // 转换成Page 类型包装
            result = page(searchHits, stringQuery.getPageable());

        } else if (isStreamQuery()) {

            if (getPageable(parameters).isUnpaged()) {
                stringQuery.setPageable(PageRequest.of(0, DEFAULT_STREAM_BATCH_SIZE));
            } else {
                stringQuery.setPageable(getPageable(parameters));
            }
            // 转成Stream 类型(内部使用滚动查询方式)
            result = StreamUtils.createStreamFromIterator(searchForStream(stringQuery, domainType, indexName));

        } else if (isCollectionQuery()) {

            if (getPageable(parameters).isPaged()) {
                stringQuery.setPageable(getPageable(parameters));
            }
            result = search(stringQuery, domainType, indexName);
        } else {

            result = searchOne(stringQuery, domainType, indexName);
        }

        // 最后解析包装类型
        return !isSearchHitMethod(this.method) ? unwrapSearchHits(result) : result;
    }

    protected Object unwrapSearchHits(Object result) {

        if (result == null) {
            return null;
        }

        if (result instanceof SearchHit<?>) {
            return ((SearchHit<?>) result).getContent();
        }

        if (result instanceof List<?>) {
            return ((List<?>) result).stream()
                    .map(this::unwrapSearchHits)
                    .collect(Collectors.toList());
        }

        if (result instanceof Page<?>) {

            Page<?> page = (Page<?>) result;
            List<?> list = page.getContent().stream().map(this::unwrapSearchHits).collect(Collectors.toList());
            return new PageImpl<>(list, page.getPageable(), page.getTotalElements(), page.getAggregations(), page.getScrollId(),
                    page.getMaxScore());
        }

        if (result instanceof Stream<?>) {
            return ((Stream<?>) result).map(this::unwrapSearchHits);
        }

        if (result instanceof SearchHits<?>) {
            SearchHits<?> searchHits = (SearchHits<?>) result;
            return unwrapSearchHits(searchHits.getSearchHits());
        }

        if (result instanceof SearchHitsIterator<?>) {
            return unwrapSearchHitsIterator((SearchHitsIterator<?>) result);
        }

        return result;
    }

    private CloseableIterator<?> unwrapSearchHitsIterator(SearchHitsIterator<?> iterator) {

        return new CloseableIterator<Object>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Object next() {
                return unwrapSearchHits(iterator.next());
            }

            @Override
            public void close() {
                iterator.close();
            }
        };
    }

    protected boolean isSearchHitMethod(Method method) {
        Class<?> methodReturnType = method.getReturnType();

        if (SearchHits.class.isAssignableFrom(methodReturnType)) {
            return true;
        }

        try {
            // dealing with Collection<SearchHit<T>>, getting to T
            ParameterizedType methodGenericReturnType = ((ParameterizedType) method.getGenericReturnType());
            if (isAllowedGenericType(methodGenericReturnType)) {
                ParameterizedType collectionTypeArgument = (ParameterizedType) methodGenericReturnType
                        .getActualTypeArguments()[0];
                if (SearchHit.class.isAssignableFrom((Class<?>) collectionTypeArgument.getRawType())) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }

        return false;
    }

    protected boolean isAllowedGenericType(ParameterizedType methodGenericReturnType) {
        return Collection.class.isAssignableFrom((Class<?>) methodGenericReturnType.getRawType())
                || Stream.class.isAssignableFrom((Class<?>) methodGenericReturnType.getRawType());
    }

    protected class ReadSearchDocumentResponseCallback<T> implements SearchDocumentResponseCallback<SearchHits<T>> {
        private final Class<T> type;
        private final DocumentCallback<T> delegate;

        public ReadSearchDocumentResponseCallback(Class<T> type, String index) {
            Assert.notNull(type, "type is null");
            this.type = type;
            // 将es的hit 数据转换成 index Class
            delegate = document -> {
                if (null == document) {
                    return null;
                }
                SimpleElasticSearchPersistentEntity<?> persistentEntity = mappingContext.getPersistentEntity(type);
                if (null != persistentEntity) {
                    SimpleElasticSearchPersistentProperty idProperty = persistentEntity.getIdProperty();
                    if (null != idProperty) {
                        document.put(idProperty.getFieldName(), document.getId());
                    }
                }
                return JSON.parseObject(JSON.toJSONString(document), type);
            };
        }

        @Override
        public SearchHits<T> doWith(SearchDocumentResponse response) {
            List<T> entities = response.getSearchDocuments().stream().map(delegate::doWith).collect(Collectors.toList());
            return mapHits(response, entities);
        }
    }

    private <T> SearchHits<T> mapHits(SearchDocumentResponse searchDocumentResponse, List<T> contents) {
        return mapHitsFromResponse(searchDocumentResponse, contents);
    }

    private <T> SearchHitsImpl<T> mapHitsFromResponse(SearchDocumentResponse searchDocumentResponse, List<T> contents) {

        Assert.notNull(searchDocumentResponse, "searchDocumentResponse is null");
        Assert.notNull(contents, "contents is null");

        Assert.isTrue(searchDocumentResponse.getSearchDocuments().size() == contents.size(),
                "Count of documents must match the count of entities");

        long totalHits = searchDocumentResponse.getTotalHits();
        float maxScore = searchDocumentResponse.getMaxScore();
        String scrollId = searchDocumentResponse.getScrollId();

        List<SearchHit<T>> searchHits = new ArrayList<>();
        List<SearchDocumentResponse.SearchDocument> searchDocuments = searchDocumentResponse.getSearchDocuments();
        for (int i = 0; i < searchDocuments.size(); i++) {
            SearchDocumentResponse.SearchDocument document = searchDocuments.get(i);
            T content = contents.get(i);
            SearchHit<T> hit = mapHit(document, content);
            searchHits.add(hit);
        }
        Aggregations aggregations = searchDocumentResponse.getAggregations();
        SearchHits.TotalHitsRelation totalHitsRelation = SearchHits.TotalHitsRelation.valueOf(searchDocumentResponse.getTotalHitsRelation());

        return new SearchHitsImpl<>(totalHits, totalHitsRelation, maxScore, scrollId, searchHits, aggregations);
    }

    private <T> SearchHit<T> mapHit(SearchDocumentResponse.SearchDocument searchDocument, T content) {

        Assert.notNull(searchDocument, "searchDocument is null");
        Assert.notNull(content, "content is null");

        return new SearchHit<>(searchDocument.getIndex(),
                searchDocument.hasId() ? searchDocument.getId() : null,
                searchDocument.getScore(),
                searchDocument.getSortValues(),
                searchDocument.getHighlightFields(),
                mapInnerHits(searchDocument),
                searchDocument.getNestedMetaData(),
                content);
    }

    private Map<String, SearchHits<?>> mapInnerHits(SearchDocumentResponse.SearchDocument searchDocument) {

        Map<String, SearchHits<?>> innerHits = new LinkedHashMap<>();
        Map<String, SearchDocumentResponse> documentInnerHits = searchDocument.getInnerHits();

        if (documentInnerHits != null && documentInnerHits.size() > 0) {

            for (Map.Entry<String, SearchDocumentResponse> entry : documentInnerHits.entrySet()) {
                SearchDocumentResponse searchDocumentResponse = entry.getValue();

                SearchHits<SearchDocumentResponse.SearchDocument> searchHits =
                        mapHitsFromResponse(searchDocumentResponse, searchDocumentResponse.getSearchDocuments());

                // map Documents to real objects
                // TODO 最后搞
//                SearchHits<?> mappedSearchHits = mapInnerDocuments(searchHits, type);
//
//                innerHits.put(entry.getKey(), mappedSearchHits);
            }

        }
        return innerHits;
    }

    protected interface DocumentCallback<T> {
        @Nullable
        T doWith(@Nullable ElasticsearchDocument document);
    }

    protected interface SearchDocumentResponseCallback<T> {
        T doWith(SearchDocumentResponse response);
    }

    /**
     * <h2> 构建Elasticsearch Query查询 </h2>
     */
    private ElasticsearchStringQuery createQuery(Object[] parameters) {
        String queryString = replacePlaceHolders(this.query, parameters);
        return new ElasticsearchStringQuery(queryString);
    }

    /**
     * <h2> 构建高亮查询 </h2>
     */
    private HighlightQuery getHighlightQuery(Highlight highlight, @Nullable Class<?> type) {

        Assert.notNull(highlight, "highlight must not be null");
        return new HighlightQuery.HighlightQueryBuilder().getHighlightQuery(highlight, type);
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


    /**
     * <h2> 将命中结果包装成分页对象Page </h2>
     */
    public <T> Page<SearchHit<T>> page(SearchHits<T> searchHits, Pageable pageable) {
        return new PageImpl<>(
                searchHits.getSearchHits(),
                pageable,
                searchHits.getTotalHits(),
                searchHits.getAggregations(),
                null,
                searchHits.getMaxScore()
        );
    }

    public <T> SearchHits<T> searchScrollStart(long scrollTimeInMillis, Query query, Class<T> clazz,
                                               String index) {

        Assert.notNull(query.getPageable(), "pageable of query must not be null.");

        SearchRequest searchRequest = createSearchRequest(query, clazz, index);
        searchRequest.scroll(TimeValue.timeValueMillis(scrollTimeInMillis));

        SearchResponse response = execute(client -> client.search(searchRequest, RequestOptions.DEFAULT));

        SearchDocumentResponseCallback<SearchHits<T>> callback = new ReadSearchDocumentResponseCallback<>(clazz,
                index);
        return callback.doWith(SearchDocumentResponse.from(response));
    }

    public <T> SearchHits<T> searchScrollContinue(@Nullable String scrollId, long scrollTimeInMillis,
                                                  Class<T> clazz, String index) {

        SearchScrollRequest request = new SearchScrollRequest(scrollId);
        request.scroll(TimeValue.timeValueMillis(scrollTimeInMillis));

        SearchResponse response = execute(client -> client.scroll(request, RequestOptions.DEFAULT));

        SearchDocumentResponseCallback<SearchHits<T>> callback = //
                new ReadSearchDocumentResponseCallback<>(clazz, index);
        return callback.doWith(SearchDocumentResponse.from(response));
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

    protected <T> SearchHit<T> searchOne(Query query, Class<T> clazz, String index) {
        List<SearchHit<T>> content = search(query, clazz, index).getSearchHits();
        return content.isEmpty() ? null : content.get(0);
    }

    protected <T> SearchHits<T> search(Query query, Class<T> type, String index) {
        SearchRequest searchRequest = createSearchRequest(query, type, index);
        SearchResponse response = execute(client -> restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT));
        // response 就是根据
        SearchDocumentResponseCallback<SearchHits<T>> searchDocumentResponseCallback =
                new ReadSearchDocumentResponseCallback<>(type, index);
        return searchDocumentResponseCallback.doWith(SearchDocumentResponse.from(response));
    }

    protected SearchRequest createSearchRequest(Query query, @Nullable Class<?> clazz, String index) {

        SearchRequest searchRequest = prepareSearchRequest(query, clazz, index);
        QueryBuilder queryBuilder = getQuery(query);
//        QueryBuilder elasticsearchFilter = getFilter(query);

        searchRequest.source().query(queryBuilder);
        return searchRequest;
    }


    private SearchRequest prepareSearchRequest(Query query, @Nullable Class<?> clazz, String index) {

        Assert.notNull(index, "No Index defined for Query!");

        SearchRequest searchRequest = new SearchRequest(index);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.version(true);

        if (query.getPageable().isPaged()) {
            sourceBuilder.from(query.getPageable().getPageNumber());
            sourceBuilder.size(query.getPageable().getPageSize());
        } else {
            sourceBuilder.from(0);
            sourceBuilder.size(INDEX_MAX_RESULT_WINDOW);
        }

        if (!query.getFields().isEmpty()) {
            sourceBuilder.fetchSource(query.getFields().toArray(new String[0]), null);
        }

        searchRequest.searchType(query.getSearchType());

        prepareSort(query, sourceBuilder);

        HighlightBuilder highlightBuilder = highlightBuilder(query);

        if (highlightBuilder != null) {
            sourceBuilder.highlighter(highlightBuilder);
        }

        if (query.getTrackTotalHits() != null) {
            sourceBuilder.trackTotalHits(query.getTrackTotalHits());
        } else if (query.getTrackTotalHitsUpTo() != null) {
            sourceBuilder.trackTotalHitsUpTo(query.getTrackTotalHitsUpTo());
        }

        if (StringUtils.hasLength(query.getRoute())) {
            searchRequest.routing(query.getRoute());
        }

        searchRequest.source(sourceBuilder);
        return searchRequest;
    }

    @SuppressWarnings("rawtypes")
    private void prepareSort(Query query, SearchSourceBuilder sourceBuilder) {

        if (query.getSort() != null) {
            query.getSort().getOrders().forEach(order -> sourceBuilder.sort(getSortBuilder(order)));
        }
    }

    private SortBuilder<?> getSortBuilder(Sort.Order order) {
        SortOrder sortOrder = order.getDirection().isDescending() ? SortOrder.DESC : SortOrder.ASC;

        if (ScoreSortBuilder.NAME.equals(order.getProperty())) {
            return SortBuilders //
                    .scoreSort() //
                    .order(sortOrder);
        } else {
            String fieldName = order.getProperty();

//            if (order instanceof GeoDistanceOrder) {
//                GeoDistanceOrder geoDistanceOrder = (GeoDistanceOrder) order;
//
//                GeoDistanceSortBuilder sort = SortBuilders.geoDistanceSort(fieldName, geoDistanceOrder.getGeoPoint().getLat(),
//                        geoDistanceOrder.getGeoPoint().getLon());
//
//                sort.geoDistance(GeoDistance.fromString(geoDistanceOrder.getDistanceType().name()));
//                sort.ignoreUnmapped(geoDistanceOrder.getIgnoreUnmapped());
//                sort.sortMode(SortMode.fromString(geoDistanceOrder.getMode().name()));
//                sort.unit(DistanceUnit.fromString(geoDistanceOrder.getUnit()));
//                return sort;
//            }
//        else{
            FieldSortBuilder sort = SortBuilders //
                    .fieldSort(fieldName) //
                    .order(sortOrder);

//            if (order.getNullHandling() == Sort.NullHandling.NULLS_FIRST) {
//                sort.missing("_first");
//            } else if (order.getNullHandling() == Sort.NullHandling.NULLS_LAST) {
//                sort.missing("_last");
//            }
            return sort;
//            }
        }
    }

    @Nullable
    public HighlightBuilder highlightBuilder(Query query) {
        HighlightBuilder highlightBuilder = query.getHighlightQuery().map(HighlightQuery::getHighlightBuilder).orElse(null);

        if (highlightBuilder == null) {

//            if (query instanceof NativeSearchQuery) {
//                NativeSearchQuery searchQuery = (NativeSearchQuery) query;
//
//                if (searchQuery.getHighlightFields() != null || searchQuery.getHighlightBuilder() != null) {
//                    highlightBuilder = searchQuery.getHighlightBuilder();
//
//                    if (highlightBuilder == null) {
//                        highlightBuilder = new HighlightBuilder();
//                    }
//
//                    if (searchQuery.getHighlightFields() != null) {
//                        for (HighlightBuilder.Field highlightField : searchQuery.getHighlightFields()) {
//                            highlightBuilder.field(highlightField);
//                        }
//                    }
//                }
//            }
        }
        return highlightBuilder;
    }


    @Nullable
    private QueryBuilder getQuery(Query query) {
        QueryBuilder elasticsearchQuery;

//        if (query instanceof NativeSearchQuery) {
//            NativeSearchQuery searchQuery = (NativeSearchQuery) query;
//            elasticsearchQuery = searchQuery.getQuery();
//        } else if (query instanceof CriteriaQuery) {
//            CriteriaQuery criteriaQuery = (CriteriaQuery) query;
//            elasticsearchQuery = new CriteriaQueryProcessor().createQuery(criteriaQuery.getCriteria());
//        }
        if (query instanceof ElasticsearchStringQuery) {
            ElasticsearchStringQuery stringQuery = (ElasticsearchStringQuery) query;
            elasticsearchQuery = wrapperQuery(stringQuery.getSource());
        } else {
            throw new IllegalArgumentException("unhandled Query implementation " + query.getClass().getName());
        }

        return elasticsearchQuery;
    }

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
     * @since 4.0
     */
    public <T> T execute(ClientCallback<T> callback) {

        Assert.notNull(callback, "callback must not be null");

        try {
            return callback.doWithClient(restHighLevelClient);
        } catch (IOException | RuntimeException e) {
            throw new RuntimeException(e);
        }
    }
}
