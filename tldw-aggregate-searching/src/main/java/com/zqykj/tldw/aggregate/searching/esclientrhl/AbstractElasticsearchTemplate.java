/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.searching.esclientrhl;

import com.alibaba.fastjson.JSON;
import com.zqykj.domain.page.Sort;
import com.zqykj.domain.routing.Routing;
import com.zqykj.tldw.aggregate.Exception.BulkFailureException;
import com.zqykj.tldw.aggregate.data.query.elasticsearch.ElasticsearchStringQuery;
import com.zqykj.tldw.aggregate.data.query.elasticsearch.HighlightQuery;
import com.zqykj.tldw.aggregate.data.query.elasticsearch.Query;
import com.zqykj.tldw.aggregate.data.query.elasticsearch.core.*;
import com.zqykj.tldw.aggregate.index.elasticsearch.ElasticsearchPersistentEntity;
import com.zqykj.tldw.aggregate.index.elasticsearch.SimpleElasticSearchPersistentEntity;
import com.zqykj.tldw.aggregate.index.elasticsearch.SimpleElasticSearchPersistentProperty;
import com.zqykj.tldw.aggregate.index.elasticsearch.SimpleElasticsearchMappingContext;
import com.zqykj.tldw.aggregate.index.mapping.PersistentProperty;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.*;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.elasticsearch.index.query.QueryBuilders.wrapperQuery;

@Slf4j
public abstract class AbstractElasticsearchTemplate {

    protected final GenericConversionService conversionService;
    protected final SimpleElasticsearchMappingContext mappingContext;
    static final Integer INDEX_MAX_RESULT_WINDOW = 10_000;

    public AbstractElasticsearchTemplate(SimpleElasticsearchMappingContext mappingContext,
                                         GenericConversionService conversionService) {
        this.mappingContext = mappingContext;
        this.conversionService = conversionService;
    }


    /**
     * <h2> 对批量操作返回进行检查 </h2>
     */
    protected void checkForBulkOperationFailure(BulkResponse bulkResponse) {

        if (bulkResponse.hasFailures()) {
            Map<String, String> failedDocuments = new HashMap<>();
            for (BulkItemResponse item : bulkResponse.getItems()) {

                if (item.isFailed())
                    failedDocuments.put(item.getId(), item.getFailureMessage());
            }
            throw new BulkFailureException(
                    "Bulk operation has failures. Use ElasticsearchException.getFailedDocuments() for detailed messages ["
                            + failedDocuments + ']',
                    failedDocuments);
        }
        // 打印bulk response index information
        Stream.of(bulkResponse.getItems()).forEach(bulkItemResponse -> {
            DocWriteResponse response = bulkItemResponse.getResponse();
            if (response != null) {
                if (log.isDebugEnabled()) {
                    log.warn("bulk response id = {}, seqNo = {}, primaryTerm = {}, version = {}",
                            response.getId(), response.getSeqNo(), response.getPrimaryTerm(), response.getVersion());
                }
            }
        });
    }

    /**
     * <h2> 批量操作请求 </h2>
     * <p> requests for {@link IndexRequest } or {@link UpdateRequest}</p>
     */
    protected BulkRequest getBulkRequest(List<?> requests, BulkOptions bulkOptions, String indexName) {
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
        requests.forEach(request -> {
            bulkRequest.add((IndexRequest) request);
        });
        return bulkRequest;
    }

    protected <T> IndexRequest getIndexRequest(T entity, String indexName) {
        IndexRequest indexRequest;
        // 判断是否有ID
        String id = getEntityId(entity);
        if (id != null) {
            indexRequest = new IndexRequest(indexName).id(id);
        } else {
            indexRequest = new IndexRequest(indexName);
        }
        indexRequest.source(JSON.toJSONString(entity), XContentType.JSON);
        String routing = getRouting(entity);
        if (routing != null) {
            indexRequest.routing(routing);
        }
        return indexRequest;
    }

    /**
     * <h2> 获取当前数据的Id </h2>
     */
    protected <T> String getEntityId(T bean) {

        SimpleElasticSearchPersistentProperty idProperty =
                mappingContext.getRequiredPersistentEntity(bean.getClass()).getIdProperty();
        if (null != idProperty) {
            Object id = getProperty(idProperty, bean);
            if (null != id) {
                return Objects.toString(id, null);
            }
        }
        return null;
    }

    /**
     * <h2> 获取路由key </h2>
     */
    protected <T> String getRouting(T bean) {

        SimpleElasticSearchPersistentProperty routingFieldProperty =
                mappingContext.getRequiredPersistentEntity(bean.getClass()).getRoutingFieldProperty();
        Routing routing = convertIfNecessary(routingFieldProperty, Routing.class);
        if (null != routing && null != routing.getRouting()) {
            return conversionService.convert(routing.getRouting(), String.class);
        }
        return null;
    }

    public <T> Object getProperty(PersistentProperty<?> property, T bean) {

        Assert.notNull(property, "PersistentProperty must not be null!");

        try {
            Field field = property.getRequiredField();

            ReflectionUtils.makeAccessible(field);
            return ReflectionUtils.getField(field, bean);

        } catch (IllegalStateException e) {
            throw new RuntimeException(
                    String.format("Could not read property %s of %s!", property.toString(), bean.toString()), e);
        }
    }

    @SuppressWarnings("unchecked")
    private <S> S convertIfNecessary(@Nullable Object source, Class<S> type) {

        return (S) (source == null //
                ? null //
                : type.isAssignableFrom(source.getClass()) //
                ? source //
                : conversionService.convert(source, type));
    }

    /**
     * <h2> 获取指定类的 index Name </h2>
     */
    public String getIndexCoordinatesFor(Class<?> clazz) {
        return getRequiredPersistentEntity(clazz).getIndexName();
    }

    ElasticsearchPersistentEntity<?> getRequiredPersistentEntity(Class<?> clazz) {
        return mappingContext.getRequiredPersistentEntity(clazz);
    }

    public interface DocumentCallback<T> {
        @Nullable
        T doWith(@Nullable ElasticsearchDocument document);
    }

    public interface SearchDocumentResponseCallback<T> {
        T doWith(SearchDocumentResponse response);
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
            FieldSortBuilder sort = SortBuilders //
                    .fieldSort(fieldName) //
                    .order(sortOrder);
            return sort;
        }
    }

    @Nullable
    public HighlightBuilder highlightBuilder(Query query) {
        return query.getHighlightQuery().map(HighlightQuery::getHighlightBuilder).orElse(null);
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
}
