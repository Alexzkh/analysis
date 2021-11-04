/**
 * @作者 Mcj
 */
package com.zqykj.core;

import com.zqykj.core.convert.ElasticsearchConverter;
import com.zqykj.core.convert.MappingElasticsearchConverter;
import com.zqykj.core.document.Document;
import com.zqykj.core.document.SearchDocumentResponse;
import com.zqykj.core.mapping.ElasticsearchPersistentEntity;
import com.zqykj.core.mapping.ElasticsearchPersistentProperty;
import com.zqykj.core.mapping.SimpleElasticsearchMappingContext;
import com.zqykj.coverter.EntityReader;
import com.zqykj.domain.Routing;
import com.zqykj.exception.BulkFailureException;
import com.zqykj.mapping.PersistentPropertyAccessor;
import com.zqykj.repository.query.BulkOptions;
import com.zqykj.repository.query.IndexQuery;
import com.zqykj.repository.query.Query;
import com.zqykj.repository.query.UpdateQuery;
import com.zqykj.util.Streamable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractElasticsearchTemplate implements DocumentOperations, SearchOperations {

    protected ElasticsearchConverter elasticsearchConverter;
    protected RequestFactory requestFactory;
    static Integer INDEX_MAX_RESULT_WINDOW = 10_000;
    static final String ID = "id";

    protected void initialize(ElasticsearchConverter elasticsearchConverter) {

        Assert.notNull(elasticsearchConverter, "elasticsearchConverter must not be null.");
        this.elasticsearchConverter = elasticsearchConverter;
        requestFactory = new RequestFactory(elasticsearchConverter);
    }

    /**
     * <h2> 构建Elasticsearch entity converter </h2>
     */
    protected ElasticsearchConverter createElasticsearchConverter() throws Exception {
        MappingElasticsearchConverter mappingElasticsearchConverter = new MappingElasticsearchConverter(
                new SimpleElasticsearchMappingContext());
        mappingElasticsearchConverter.afterPropertiesSet();
        return mappingElasticsearchConverter;
    }

    public ElasticsearchConverter getElasticsearchConverter() {

        Assert.notNull(elasticsearchConverter, "elasticsearchConverter is not initialized.");

        return elasticsearchConverter;
    }

    public RequestFactory getRequestFactory() {

        Assert.notNull(requestFactory, "requestFactory not initialized");

        return requestFactory;
    }

    /**
     * <h2> 给定一个索引类,创建对应的索引操作类 </h2>
     */
    public abstract ElasticsearchIndexOperations indexOps(Class<?> clazz);

    /**
     * <h2> 给定一个索引类名称 </h2>
     */
    public abstract ElasticsearchIndexOperations indexOps(String index);

    @Override
    public <T> T save(T entity, String routing) {

        Assert.notNull(entity, "entity must not be null");

        return save(entity, getIndexCoordinatesFor(entity.getClass()), routing);
    }

    public <T> T save(T entity, String index, String routing) {

        Assert.notNull(entity, "entity must not be null");
        Assert.notNull(index, "index must not be null");
        IndexQuery indexQuery = getIndexQuery(entity, routing);
        //
        doIndex(indexQuery, index);

        return entity;
    }

    @Override
    public <T> Iterable<T> save(Iterable<T> entities, String routing) {

        Assert.notNull(entities, "entities must not be null");

        Iterator<T> iterator = entities.iterator();
        if (iterator.hasNext()) {
            return save(entities, getIndexCoordinatesFor(iterator.next().getClass()), routing);
        }

        return entities;
    }

    @Override
    public <T> Iterable<T> save(Iterable<T> entities, String index, String routing) {

        Assert.notNull(entities, "entities must not be null");
        Assert.notNull(index, "index must not be null");

        List<IndexQuery> indexQueries = Streamable.of(entities).stream().map(entity -> getIndexQuery(entity, routing))
                .collect(Collectors.toList());

        if (!indexQueries.isEmpty()) {
            bulkIndex(indexQueries, index);
        }

        return entities;
    }

    @Override
    public List<Map<String, ?>> save(List<Map<String, ?>> values, String indexName, String routing) {

        Assert.notNull(values, "values must not be null");
        Assert.notNull(indexName, "indexName must not be null");

        List<IndexQuery> indexQueries = Streamable.of(values).stream().map(value -> getIndexQuery(value, routing)).collect(Collectors.toList());

        if (!indexQueries.isEmpty()) {
            bulkIndex(indexQueries, indexName);
        }
        return values;
    }

    @Override
    public void bulkIndex(List<IndexQuery> queries, Class<?> clazz) {
        bulkIndex(queries, getIndexCoordinatesFor(clazz));
    }

    @Override
    public final void bulkIndex(List<IndexQuery> queries, BulkOptions bulkOptions,
                                String index) {

        Assert.notNull(queries, "List of IndexQuery must not be null");
        Assert.notNull(bulkOptions, "BulkOptions must not be null");

        bulkOperation(queries, bulkOptions, index);
    }

    @Override
    public void bulkUpdate(List<UpdateQuery> queries, Class<?> clazz) {
        bulkUpdate(queries, getIndexCoordinatesFor(clazz));
    }

    @Override
    public void bulkUpdate(List<UpdateQuery> queries, BulkOptions bulkOptions, String index) {

        Assert.notNull(queries, "List of UpdateQuery must not be null");
        Assert.notNull(bulkOptions, "BulkOptions must not be null");

        doBulkOperation(queries, bulkOptions, index);
    }

    public void bulkOperation(List<?> queries, BulkOptions bulkOptions,
                              String index) {

        Assert.notNull(queries, "List of IndexQuery must not be null");
        Assert.notNull(bulkOptions, "BulkOptions must not be null");

        doBulkOperation(queries, bulkOptions, index);
    }

    public abstract void doBulkOperation(List<?> queries, BulkOptions bulkOptions,
                                         String index);

    @Override
    public long count(Query query, Class<?> clazz) {
        return count(query, clazz, getIndexCoordinatesFor(clazz));
    }

    public abstract String doIndex(IndexQuery query, String index);

    private <T> IndexQuery getIndexQuery(T entity, String routing) {

        if (entity instanceof Map) {
            Map<String, ?> value = (Map<String, ?>) entity;
            return getIndexQuery(value, routing);
        }
        IndexQuery indexQuery = new IndexQuery();
        String id = getEntityId(entity);

        indexQuery.setId(id);
        indexQuery.setObject(entity);
        //TODO version cannot be used together with seq_no and primary_term

        if (StringUtils.isBlank(routing)) {
            routing = getEntityRouting(entity);
        }
        if (null != getEntityVersion(entity)) {
            indexQuery.setVersion(getEntityVersion(entity));
        }
        if (StringUtils.isNotBlank(routing)) {
            indexQuery.setRouting(routing);
        }
        return indexQuery;
    }

    private IndexQuery getIndexQuery(Map<String, ?> value, String routing) {
        IndexQuery indexQuery = new IndexQuery();
        String id = null != value.get(ID) ? value.get(ID).toString() : null;
        indexQuery.setId(id);
        indexQuery.setSourceMap(value);
        if (StringUtils.isNotBlank(routing)) {
            indexQuery.setRouting(routing);
        }
        return indexQuery;
    }

    @Override
    public <T> List<T> multiGet(Query query, Class<T> clazz) {
        return multiGet(query, clazz, getIndexCoordinatesFor(clazz));
    }

    @Override
    public <T> SearchHits<T> search(Query query, Class<T> clazz) {
        return search(query, clazz, getIndexCoordinatesFor(clazz));
    }

    @Nullable
    private Long getEntityVersion(Object entity) {

        ElasticsearchPersistentEntity<?> requiredPersistentEntity = getRequiredPersistentEntity(entity.getClass());

        if (null != requiredPersistentEntity.getVersionProperty()) {
            Object version = requiredPersistentEntity.getPropertyAccessor(entity).getProperty(requiredPersistentEntity.getVersionProperty());

            if (version != null && Long.class.isAssignableFrom(version.getClass())) {
                return ((Long) version);
            }
        }

        return null;
    }

    /**
     * <h2> 仅供内部使用，不适用于公共 API </h2>
     */
    abstract protected <T> SearchScrollHits<T> searchScrollStart(long scrollTimeInMillis, Query query, Class<T> clazz,
                                                                 String index);

    /**
     * <h2> 仅供内部使用，不适用于公共 API </h2>
     */
    abstract protected <T> SearchScrollHits<T> searchScrollContinue(@Nullable String scrollId, long scrollTimeInMillis,
                                                                    Class<T> clazz, String index);

    /**
     * <h2> 仅供内部使用，不适用于公共 API </h2>
     */
    protected void searchScrollClear(String scrollId) {
        searchScrollClear(Collections.singletonList(scrollId));
    }

    /**
     * <h2> 仅供内部使用，不适用于公共 API </h2>
     */
    abstract protected void searchScrollClear(List<String> scrollIds);


    /**
     * <h2> 获取当前索引类ID </h2>
     */
    @Nullable
    private String getEntityId(Object bean) {

        ElasticsearchPersistentEntity<?> entity = getRequiredPersistentEntity(bean.getClass());
        if (null == entity) {
            return null;
        }
        Object id = entity.getIdentifierAccessor(bean);
        if (id != null) {
            return Objects.toString(id, null);
        }

        return null;
    }

    /**
     * <h2> 获取当前索引类上设置的routing </h2>
     */
    @Nullable
    public String getEntityRouting(Object bean) {
        ElasticsearchPersistentEntity<?> entity = getRequiredPersistentEntity(bean.getClass());
        PersistentPropertyAccessor<Object> propertyAccessor = entity.getPropertyAccessor(bean);
        return getRouting(entity, propertyAccessor);
    }

    public String getRouting(ElasticsearchPersistentEntity<?> entity, PersistentPropertyAccessor<Object> propertyAccessor) {

        ElasticsearchPersistentProperty routingFieldProperty = entity.getRoutingFieldProperty();

        if (routingFieldProperty != null) {
            Routing routingField = (Routing) propertyAccessor.getProperty(routingFieldProperty);

            if (routingField != null && routingField.getName() != null) {
                return elasticsearchConverter.getConversionService().convert(routingField.getName(), String.class);
            }
        }
        return null;
    }

    /**
     * <h2> 获取索引类名称 </h2>
     *
     * @param clazz the entity class
     * @return the Index defined on the entity.
     */
    public String getIndexCoordinatesFor(Class<?> clazz) {
        return getRequiredPersistentEntity(clazz).getIndexName();
    }

    ElasticsearchPersistentEntity<?> getRequiredPersistentEntity(Class<?> clazz) {
        return elasticsearchConverter.getMappingContext().getRequiredPersistentEntity(clazz);
    }

    /**
     * <h2> 检查批量操作是否失败  </h2>
     *
     * @param bulkResponse 批量操作返回
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
    }

    /**
     * <h2> 对 elasticsearch 返回的document 回调处理 (如读取文档处理) </h2>
     */
    protected interface DocumentCallback<T> {
        @Nullable
        T doWith(@Nullable Document document);
    }

    /**
     * <h2> 读取文档处理(elasticsearch search 返回的hits (一个document 相当于 一个hit) </h2>
     */
    protected class ReadDocumentCallback<T> implements DocumentCallback<T> {
        private final EntityReader<? super T, Document> reader;
        private final Class<T> type;

        public ReadDocumentCallback(EntityReader<? super T, Document> reader, Class<T> type) {

            Assert.notNull(reader, "reader is null");
            Assert.notNull(type, "type is null");

            this.reader = reader;
            this.type = type;
        }

        @Nullable
        public T doWith(@Nullable Document document) {

            if (document == null) {
                return null;
            }

            T entity = reader.read(type, document);
            return entity;
        }
    }

    /**
     * <h2> 搜索文档相应回调 </h2>
     */
    public interface SearchDocumentResponseCallback<T> {
        @NonNull
        T doWith(@NonNull SearchDocumentResponse response);
    }

    /**
     * <h2> 读取搜索文档相应回调 </h2>
     */
    public class ReadSearchDocumentResponseCallback<T> implements SearchDocumentResponseCallback<SearchHits<T>> {
        private final DocumentCallback<T> delegate;
        private final Class<T> type;

        public ReadSearchDocumentResponseCallback(Class<T> type) {

            Assert.notNull(type, "type is null");

            this.delegate = new ReadDocumentCallback<>(elasticsearchConverter, type);
            this.type = type;
        }

        @Override
        public SearchHits<T> doWith(SearchDocumentResponse response) {
            List<T> entities = response.getSearchDocuments().stream().map(delegate::doWith).collect(Collectors.toList());
            return SearchHitMapping.mappingFor(type, elasticsearchConverter).mapHits(response, entities);
        }
    }

    /**
     * <h2> 读取搜索滚动文档响应回调 </h2>
     */
    protected class ReadSearchScrollDocumentResponseCallback<T>
            implements SearchDocumentResponseCallback<SearchScrollHits<T>> {
        private final DocumentCallback<T> delegate;
        private final Class<T> type;

        public ReadSearchScrollDocumentResponseCallback(Class<T> type) {

            Assert.notNull(type, "type is null");

            this.delegate = new ReadDocumentCallback<>(elasticsearchConverter, type);
            this.type = type;
        }

        @Override
        public SearchScrollHits<T> doWith(SearchDocumentResponse response) {
            List<T> entities = response.getSearchDocuments().stream().map(delegate::doWith).collect(Collectors.toList());
            return SearchHitMapping.mappingFor(type, elasticsearchConverter).mapScrollHits(response, entities);
        }
    }

    // 聚合解析
}
