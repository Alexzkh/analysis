/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.data.query.elasticsearch;

import com.zqykj.domain.page.*;
import com.zqykj.infrastructure.util.CloseableIterator;
import com.zqykj.infrastructure.util.StreamUtils;
import com.zqykj.tldw.aggregate.data.query.elasticsearch.core.SearchHitsIterator;
import com.zqykj.annotations.Highlight;
import com.zqykj.tldw.aggregate.data.query.AbstractAggregateRepositoryQuery;
import com.zqykj.tldw.aggregate.data.query.AggregateRepositoryQuery;
import com.zqykj.tldw.aggregate.data.query.elasticsearch.core.*;
import com.zqykj.tldw.aggregate.data.repository.RepositoryInformation;
import com.zqykj.tldw.aggregate.index.elasticsearch.SimpleElasticsearchMappingContext;
import com.zqykj.tldw.aggregate.searching.esclientrhl.ElasticsearchRestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <h1> Elasticsearch 处理@Query 注解的query <h1/>
 */
@Slf4j
public class AggregateElasticsearchRepositoryStringQuery extends AbstractAggregateRepositoryQuery
        implements AggregateRepositoryQuery {

    private final ElasticsearchRestTemplate restTemplate;
    private final RepositoryInformation repositoryInformation;
    private final SimpleElasticsearchMappingContext mappingContext;
    private final String query;
    protected static final int DEFAULT_STREAM_BATCH_SIZE = 500;

    public AggregateElasticsearchRepositoryStringQuery(ElasticsearchRestTemplate elasticsearchRestTemplate,
                                                       RepositoryInformation repositoryInformation,
                                                       Method method,
                                                       String query) {
        super(method, repositoryInformation);
        Assert.notNull(elasticsearchRestTemplate, "Elasticsearch rest template cannot be empty!");
        this.repositoryInformation = repositoryInformation;
        this.restTemplate = elasticsearchRestTemplate;
        this.mappingContext = elasticsearchRestTemplate.getMappingContext();
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
        // 查询的时候添加routing
        stringQuery.setRoute(getRouting(parameters).getRouting());
        // 判断是Page 分页查询、stream流查询、collection查询、普通查询方式
        if (isPageQuery()) {

            // 如果method 有分页参数需要设置的话
            stringQuery.setPageable(getPageable(parameters));
            // 使用client 进行查询
            SearchHits<?> searchHits = restTemplate.search(stringQuery, domainType, indexName);
            // 转换成Page 类型包装
            result = page(searchHits, stringQuery.getPageable());

        } else if (isStreamQuery()) {

            if (getPageable(parameters).isUnpaged()) {
                stringQuery.setPageable(PageRequest.of(0, DEFAULT_STREAM_BATCH_SIZE));
            } else {
                stringQuery.setPageable(getPageable(parameters));
            }
            // 转成Stream 类型(内部使用滚动查询方式)
            result = StreamUtils.createStreamFromIterator(restTemplate.searchForStream(stringQuery, domainType, indexName));

        } else if (isCollectionQuery()) {

            if (getPageable(parameters).isPaged()) {
                stringQuery.setPageable(getPageable(parameters));
            }
            result = restTemplate.search(stringQuery, domainType, indexName);
        } else {

            result = restTemplate.searchOne(stringQuery, domainType, indexName);
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
}
