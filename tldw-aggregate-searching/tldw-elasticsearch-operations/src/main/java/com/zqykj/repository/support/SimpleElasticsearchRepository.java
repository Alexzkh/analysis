/**
 * @作者 Mcj
 */
package com.zqykj.repository.support;


import com.zqykj.common.enums.QueryType;
import com.zqykj.common.es.BuilderQuery;
import com.zqykj.common.request.AggregateBuilder;
import com.zqykj.common.request.DateHistogramBuilder;
import com.zqykj.common.request.QueryParams;
import com.zqykj.common.response.ParsedStats;
import com.zqykj.core.aggregation.factory.AggregateRequestFactory;
import com.zqykj.core.aggregation.parse.AggregationParser;
import com.zqykj.core.aggregation.query.builder.AggregationMappingBuilder;
import com.zqykj.core.aggregation.query.builder.QueryMappingBuilder;
import com.zqykj.parameters.Pagination;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.aggregate.date.DateSpecificFormat;
import com.zqykj.parameters.query.QuerySpecialParams;
import com.zqykj.domain.*;
import com.zqykj.repository.util.DateHistogramIntervalUtil;
import com.zqykj.support.ParseAggregationResultUtil;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.*;
import org.elasticsearch.search.aggregations.bucket.histogram.*;
import org.elasticsearch.search.aggregations.bucket.range.ParsedRange;
import org.elasticsearch.search.aggregations.bucket.range.RangeAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Stats;
import com.zqykj.core.*;
import com.zqykj.core.aggregation.AggregatedPage;
import com.zqykj.core.mapping.ElasticsearchPersistentEntity;
import com.zqykj.core.mapping.ElasticsearchPersistentProperty;
import com.zqykj.core.query.NativeSearchQueryBuilder;
import com.zqykj.enums.AggsType;
import com.zqykj.enums.DateIntervalUnit;
import com.zqykj.mapping.context.MappingContext;
import com.zqykj.repository.EntranceRepository;
import com.zqykj.repository.query.NativeSearchQuery;
import com.zqykj.repository.query.Query;
import com.zqykj.util.StreamUtils;
import com.zqykj.util.Streamable;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.*;
import org.elasticsearch.search.aggregations.pipeline.BucketSortPipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.support.ValuesSourceAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * <h1>  Elasticsearch specific repository
 * implementation. Likely to be used as target within </h1>
 *
 * @author zhangkehou
 * @author machengjun
 */
@Slf4j
@SuppressWarnings({"all"})
public class SimpleElasticsearchRepository implements EntranceRepository {

    private ElasticsearchRestTemplate operations;
    private IndexOperations indexOperations;

    MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext;


    public SimpleElasticsearchRepository(ElasticsearchRestTemplate operations) {
        this.operations = operations;
        this.mappingContext = operations.getElasticsearchConverter().getMappingContext();
        this.indexOperations = new ElasticsearchIndexOperations(operations);
    }

    private static final String WILDCARD = ".*";


    @Override
    public <T, ID> Optional<T> findById(ID id, String routing, @NonNull Class<T> entityClass) throws Exception {

        return Optional.ofNullable(
                execute(operations -> operations.get(stringIdRepresentation(id), entityClass, getIndexCoordinates(entityClass), routing),
                        entityClass
                ));
    }

    @Override
    public <T> Iterable<T> findAll(String routing, @NonNull Class<T> entityClass) {

        int itemCount = (int) this.count(routing, entityClass);

        if (itemCount == 0) {
            return new PageImpl<>(Collections.emptyList());
        }
        return this.findAll(PageRequest.of(0, Math.max(1, itemCount)), routing, entityClass);
    }

    public <T> Iterable<T> findAll(@NonNull Class<T> entityClass, @Nullable String routing, @Nullable QuerySpecialParams condition) {

        if (null != condition) {

            int itemCount = (int) this.count(routing, entityClass, condition);
            if (itemCount == 0) {
                return new PageImpl<>(Collections.emptyList());
            }
            return this.findAll(PageRequest.of(0, Math.max(1, itemCount)), routing, entityClass, condition);
        }

        return findAll(routing, entityClass);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Page<T> findAll(Pageable pageable, String routing, @NonNull Class<T> entityClass) {

        NativeSearchQuery query = new NativeSearchQueryBuilder().withQuery(matchAllQuery()).withPageable(pageable).build();

        SearchHits<T> searchHits = execute(operations -> operations.search(query, entityClass, getIndexCoordinates(entityClass)), entityClass);

        AggregatedPage<SearchHit<T>> page = SearchHitSupport.page(searchHits, query.getPageable());
        return (Page<T>) SearchHitSupport.unwrapSearchHits(page);
    }

    public <T> Page<T> findAll(Pageable pageable, @Nullable String routing, @NonNull Class<T> entityClass, @Nullable QuerySpecialParams condition) {

        if (null != condition) {

            QueryBuilder queryBuilder = (QueryBuilder) QueryMappingBuilder.buildDslQueryBuilderMapping(condition);
            NativeSearchQuery query = new NativeSearchQueryBuilder().withQuery(queryBuilder).withPageable(pageable).build();
            SearchHits<T> searchHits = execute(operations -> operations.search(query, entityClass, getIndexCoordinates(entityClass)), entityClass);

            AggregatedPage<SearchHit<T>> page = SearchHitSupport.page(searchHits, query.getPageable());
            return (Page<T>) SearchHitSupport.unwrapSearchHits(page);
        }
        return findAll(pageable, routing, entityClass);
    }

    @Override
    public <T, ID> Iterable<T> findAllById(Iterable<ID> ids, String routing, @NonNull Class<T> entityClass) {

        Assert.notNull(ids, "ids can't be null.");

        List<T> result = new ArrayList<>();
        List<String> stringIds = stringIdsRepresentation(ids);

        if (stringIds.isEmpty()) {
            return result;
        }

        NativeSearchQuery query = new NativeSearchQueryBuilder().withIds(stringIds).withRoute(routing).build();
        List<T> multiGetEntities = execute(operations -> operations.multiGet(query, entityClass, getIndexCoordinates(entityClass)), entityClass);

        if (multiGetEntities != null) {
            multiGetEntities.forEach(entity -> {

                if (entity != null) {
                    result.add(entity);
                }
            });
        }

        return result;
    }

    @Override
    public <T> T query(QueryParams queryParams, Class<T> entityClass) {

        ElasticsearchPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(entityClass);
        String aliasFieldName = entity.getRequiredPersistentProperty(queryParams.getField()).getFieldName();
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                .withQuery(termsQuery(aliasFieldName, queryParams.getValue()))
                .withPageable(PageRequest.of(0, 1))
                .build();
        String indexCoordinates = getIndexCoordinates(entityClass);
        return execute(operations -> operations.searchOne(nativeSearchQuery, entityClass, getIndexCoordinates(entityClass)), entityClass) != null ? operations.searchOne(nativeSearchQuery, entityClass, getIndexCoordinates(entityClass)).getContent() : null;
    }

    private <ID> List<String> stringIdsRepresentation(Iterable<ID> ids) {

        Assert.notNull(ids, "ids can't be null.");

        return StreamUtils.createStreamFromIterator(ids.iterator()).map(this::stringIdRepresentation)
                .collect(Collectors.toList());
    }

    @Nullable
    private <ID> String stringIdRepresentation(@Nullable ID id) {
        return operations.stringIdRepresentation(id);
    }

    @Override
    public <T> long count(String routing, @NonNull Class<T> entityClass) {

        NativeSearchQuery query = new NativeSearchQueryBuilder().withQuery(matchAllQuery())
                .withRoute(routing)
                .build();
        // noinspection ConstantConditions
        return execute(operations -> operations.count(query, entityClass, getIndexCoordinates(entityClass)), entityClass);
    }

    @Override
    public <T> long count(String routing, @NonNull Class<T> entityClass, QuerySpecialParams condition) {


        if (null != condition) {

            // 构建QueryBuilder
            QueryBuilder queryBuilder = (QueryBuilder) QueryMappingBuilder.buildDslQueryBuilderMapping(condition);

            Pagination pagination = condition.getPagination();
            return operations.conditionalQuery(queryBuilder, routing, getIndexCoordinates(entityClass),
                    true, Pageable.unpaged()).getHits().getTotalHits().value;
        }
        return count(routing, entityClass);
    }

    @Override
    public <T> T save(T entity, String routing, @NonNull Class<T> entityClass) {

        Assert.notNull(entity, "Cannot save 'null' entity.");
        return executeAndRefresh(operations -> operations.save(entity, getIndexCoordinates(entityClass), routing), entityClass);
    }

    public <T> Iterable<T> save(Iterable<T> entities, String routing, Class<T> entityClass) {

        Assert.notNull(entities, "Cannot insert 'null' as a List.");

        return Streamable.of(saveAll(entities, routing, entityClass)).stream().collect(Collectors.toList());
    }

    @Override
    public <T> Iterable<T> saveAll(Iterable<T> entities, String routing, @NonNull Class<T> entityClass) {

        Assert.notNull(entities, "Cannot insert 'null' as a List.");

        String indexCoordinates = getIndexCoordinates(entityClass);
        executeAndRefresh(operations -> operations.save(entities, indexCoordinates, routing), entityClass);

        return entities;
    }

    public <T> void saveAll(List<Map<String, ?>> values, @Nullable String routing, @NonNull Class<T> entityClass) {

        Assert.notNull(values, "Cannot insert 'null' as a List.");
        String indexCoordinates = getIndexCoordinates(entityClass);
        executeAndRefresh(operations -> operations.save(values, indexCoordinates, routing), entityClass);
    }


    @Override
    public <T, ID> void deleteById(ID id, String routing, @NonNull Class<T> entityClass) {

        Assert.notNull(id, "Cannot delete entity with id 'null'.");
        doDelete(id, getIndexCoordinates(entityClass), routing, entityClass);
    }

    private <T, ID> void doDelete(@Nullable ID id, @Nullable String routing, String indexCoordinates, Class<T> entityClass) {

        if (id != null) {
            executeAndRefresh(operations -> operations.delete(stringIdRepresentation(id), routing, indexCoordinates), entityClass);
        }
    }

    @Override
    public <T, ID> void deleteAll(Iterable<ID> ids, String routing, @NonNull Class<T> entityClass) {

        Assert.notNull(ids, "Cannot delete 'null' list.");

        String indexCoordinates = getIndexCoordinates(entityClass);
        IdsQueryBuilder idsQueryBuilder = idsQuery();
        for (ID id : ids) {
            if (id != null) {
                idsQueryBuilder.addIds(stringIdRepresentation(id));
            }
        }

        if (idsQueryBuilder.ids().isEmpty()) {
            return;
        }

        Query query = new NativeSearchQueryBuilder().withQuery(idsQueryBuilder).build();

        executeAndRefresh((OperationsCallback<Void>) operations -> {
            operations.delete(query, entityClass, indexCoordinates);
            return null;
        }, entityClass);
    }

    @Override
    public <T> void deleteAll(String routing, @NonNull Class<T> entityClass) {

        String indexCoordinates = getIndexCoordinates(entityClass);
        Query query = new NativeSearchQueryBuilder().withQuery(matchAllQuery())
                .withRoute(routing)
                .build();

        executeAndRefresh((OperationsCallback<Void>) operations -> {
            operations.delete(query, entityClass, indexCoordinates);
            return null;
        }, entityClass);
    }

    public <T> void deleteAll(@Nullable String routing, @NonNull Class<T> entityClass, QuerySpecialParams condition) {

        if (null != condition) {

            QueryBuilder queryBuilder = (QueryBuilder) QueryMappingBuilder.buildDslQueryBuilderMapping(condition);

            Query query = new NativeSearchQueryBuilder().withQuery(queryBuilder)
                    .withRoute(routing)
                    .build();

            executeAndRefresh((OperationsCallback<Void>) operations -> {
                operations.delete(query, entityClass, getIndexCoordinates(entityClass));
                return null;
            }, entityClass);
            return;
        } else {
            deleteAll(routing, entityClass);
        }
    }

    @Override
    public <T> Page<T> findByCondition(Pageable pageable, String routing, Class<T> entityClass, String... values) {
        ElasticsearchPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(entityClass);
        String propertyName = entity.getRequiredPersistentProperty(pageable.getSort().getOrders().get(0).getProperty()).getFieldName();
        if (StringUtils.isEmpty(propertyName)) {
            throw new ElasticsearchException("The sort field cannot be null or empty,but it is null or empty at this time!");
        }
        String indexName = getIndexCoordinates(entityClass);
        SearchRequest searchRequest = new SearchRequest(indexName);
        QueryBuilder boolQueryBuilder = BuilderQuery.build(values);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.sort(propertyName, pageable.getSort().getOrders().get(0).getDirection().isDescending()
                ? SortOrder.DESC : SortOrder.ASC);
        searchSourceBuilder.from(pageable.getPageNumber() * pageable.getPageSize());
        searchSourceBuilder.size(pageable.getPageSize());
        searchRequest.source(searchSourceBuilder);
        searchRequest.routing(routing);
        SearchHits<T> searchHits = execute(operations -> operations.search(searchRequest, entityClass,
                getIndexCoordinates(entityClass)), entityClass);
        AggregatedPage<SearchHit<T>> page = SearchHitSupport.page(searchHits, pageable);
        return (Page<T>) SearchHitSupport.unwrapSearchHits(page);
    }

    /**
     * <h2> 刷新索引 </h2>
     */
    public <T> void refresh(Class<T> entityClass) {
        indexOperations.refresh(entityClass);
    }

    /**
     * <h2> 自动构建索引与映射 </h2>
     */
    public <T> void createIndexAndMapping(Class<T> entityClass) {
        try {
            if (operations.shouldCreateIndexAndMapping(entityClass) && !indexOperations.exists(entityClass)) {
                indexOperations.create(entityClass);
                indexOperations.putMapping(entityClass);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            log.warn("Cannot create index: {}", exception.getMessage());
        }
    }

    private <T> String getIndexCoordinates(@NonNull Class<T> entityClass) {
        return operations.getIndexCoordinatesFor(entityClass);
    }

    @Override
    public <T> Double metricsAggs(String metricsName, AggsType aggsType, String routing, Class<T> clazz, String... indexes) {
        String[] indexNames = indexes;
        ElasticsearchPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(clazz);
        String aliasName = entity.getPersistentPropertyWithFieldName(metricsName).getFieldName();
        StringJoiner stringJoiner = new StringJoiner("_");
        stringJoiner.add(aggsType.toString());
        stringJoiner.add(aliasName);
        // the name of aggregation
        String name = stringJoiner.toString();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BiFunction<String, String, ValuesSourceAggregationBuilder> result = DateHistogramIntervalUtil.map.get(AggsType.count);
        if (result != null) {
            searchSourceBuilder.aggregation(result.apply(name, aliasName));
        }
        searchSourceBuilder.size(0);
        SearchRequest searchRequest = new SearchRequest(indexNames);
        if (StringUtils.isNotEmpty(routing)) {
            searchRequest.routing(routing);
        }
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = operations.execute(client -> client.search(searchRequest, RequestOptions.DEFAULT));
        if (AggsType.count == aggsType) {
            ValueCount count = searchResponse.getAggregations().get(name);
            long value = count.getValue();
            return Double.parseDouble(String.valueOf(value));
        } else if (AggsType.min == aggsType) {
            ParsedMin min = searchResponse.getAggregations().get(name);
            double value = min.getValue();
            return value;
        } else if (AggsType.max == aggsType) {
            ParsedMax max = searchResponse.getAggregations().get(name);
            double value = max.getValue();
            return value;
        } else if (AggsType.sum == aggsType) {
            ParsedSum sum = searchResponse.getAggregations().get(name);
            double value = sum.getValue();
            return value;
        } else if (AggsType.avg == aggsType) {
            ParsedAvg avg = searchResponse.getAggregations().get(name);
            double value = avg.getValue();
            return value;
        }
        return 0d;
    }

    @Override
    public <T> ParsedStats statsAggs(String metricsName, String routing, Class<T> clazz, String... indexes) {
        String[] indexName = indexes;
        String name = "stats";
        ElasticsearchPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(clazz);
        String aliasName = entity.getPersistentPropertyWithFieldName(metricsName).getFieldName();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        StatsAggregationBuilder aggregationBuilder = AggregationBuilders.stats(name).field(aliasName);
        searchSourceBuilder.aggregation(aggregationBuilder);
        SearchRequest searchRequest = new SearchRequest(indexName);
        if (StringUtils.isNotEmpty(routing)) {
            searchRequest.routing(routing);
        }
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = operations.execute(client -> client.search(searchRequest, RequestOptions.DEFAULT));
        Stats stats = searchResponse.getAggregations().get(name);
        ParsedStats parsedStats = ParsedStats.builder()
                .avg(stats.getAvg())
                .count(stats.getCount())
                .max(stats.getMax())
                .min(stats.getMin())
                .sum(stats.getSum())
                .build();
        return parsedStats;
    }

    @Override
    public <T> Map<String, ParsedStats> statsAggs(String metricsName, String routing, Class<T> clazz, String bucketName, String... indexes) {
        String[] indexName = indexes;
        ElasticsearchPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(clazz);
        String metricsAliasName = entity.getPersistentPropertyWithFieldName(metricsName).getFieldName();
        String bucketAliasName = entity.getPersistentPropertyWithFieldName(bucketName).getFieldName();
        // the name of aggregation
        String bucket = "by_" + bucketAliasName;
        String metric = "stats" + metricsName;
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms(bucket).field(bucketAliasName);
        termsAggregationBuilder.order(BucketOrder.count(false));
        termsAggregationBuilder.subAggregation(AggregationBuilders.stats(metricsName)
                .field(metricsAliasName))
                .size(Integer.MAX_VALUE);
        SearchRequest searchRequest = new SearchRequest(indexName);
        if (StringUtils.isNotEmpty(routing)) {
            searchRequest.routing(routing);
        }
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = operations.execute(client -> client.search(searchRequest, RequestOptions.DEFAULT));
        Aggregations aggregation = searchResponse.getAggregations();
        Terms terms = aggregation.get(bucket);
        Map<String, ParsedStats> result = new HashMap<>();
        terms.getBuckets().stream().forEach(buckets -> {
            Stats stats = buckets.getAggregations().get(metric);
            ParsedStats parsedStats = ParsedStats.builder()
                    .avg(stats.getAvg())
                    .count(stats.getCount())
                    .max(stats.getMax())
                    .min(stats.getMin())
                    .sum(stats.getSum())
                    .build();
            result.put(buckets.getKey().toString(), parsedStats);
        });
        return result;
    }

    @Override
    public <T> Map<Double, Double> percentilesAggs(String metricsName, String routing, Class<T> clazz, double[] customSegment, String... indexes) {
        ElasticsearchPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(clazz);
        String fieldName = entity.getPersistentPropertyWithFieldName(metricsName).getFieldName();
        String name = "precentiles_" + fieldName;
        PercentilesAggregationBuilder percentilesAggregationBuilder = AggregationBuilders.percentiles(name)
                .field(fieldName).percentiles(customSegment);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(0);
        searchSourceBuilder.aggregation(percentilesAggregationBuilder);
        SearchRequest searchRequest = new SearchRequest(indexes);
        if (StringUtils.isNotEmpty(routing)) {
            searchRequest.routing(routing);
        }
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = operations.execute(client -> client.search(searchRequest, RequestOptions.DEFAULT));
        Map<Double, Double> map = new LinkedHashMap<>();
        Percentiles agg = searchResponse.getAggregations().get(name);
        for (Percentile entry : agg) {
            double percent = entry.getPercent();
            double value = entry.getValue();
            map.put(percent, value);
        }
        return map;
    }

    @Override
    public <T> Map<Double, Double> percentilesRanksAggs(String metricsName, String routing, Class<T> clazz, double[] customSegment, String... indexes) {
        ElasticsearchPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(clazz);
        String fieldName = entity.getPersistentPropertyWithFieldName(metricsName).getFieldName();
        String name = "precentilesRanks_" + fieldName;
        PercentilesAggregationBuilder percentilesAggregationBuilder = AggregationBuilders.percentiles(name)
                .field(fieldName).percentiles(customSegment);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(0);
        searchSourceBuilder.aggregation(percentilesAggregationBuilder);
        SearchRequest searchRequest = new SearchRequest(indexes);
        if (StringUtils.isNotEmpty(routing)) {
            searchRequest.routing(routing);
        }
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = operations.execute(client -> client.search(searchRequest, RequestOptions.DEFAULT));
        Map<Double, Double> map = new LinkedHashMap<>();
        PercentileRanks agg = searchResponse.getAggregations().get(name);
        for (Percentile entry : agg) {
            double percent = entry.getPercent();
            double value = entry.getValue();
            map.put(percent, value);
        }
        return map;
    }

    @Override
    public <T> Map histogramAggs(String metricsName, String routing, AggsType aggsType, Class<T> clazz, String bucketName, double interval, String... indexes) {
        ElasticsearchPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(clazz);
        String metricsAliasName = entity.getPersistentPropertyWithFieldName(metricsName).getFieldName();
        String bucketAliasName = entity.getPersistentPropertyWithFieldName(bucketName).getFieldName();
        // the name of aggregation
        String bucket = "by_" + bucketAliasName;
        StringJoiner stringJoiner = new StringJoiner("_");
        stringJoiner.add(aggsType.toString());
        stringJoiner.add(metricsAliasName);
        // the name of aggregation
        String name = stringJoiner.toString();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        AggregationBuilder aggregation = AggregationBuilders.histogram(bucket).field(bucketAliasName).interval(interval);
        searchSourceBuilder.size(0);
        BiFunction<String, String, ValuesSourceAggregationBuilder> result = DateHistogramIntervalUtil.map.get(AggsType.count);
        if (result != null) {
            searchSourceBuilder.aggregation(result.apply(name, metricsAliasName));
        }
        searchSourceBuilder.aggregation(aggregation);
        SearchRequest searchRequest = new SearchRequest();
        if (StringUtils.isNotEmpty(routing)) {
            searchRequest.routing(routing);
        }
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = operations.execute(client -> client.search(searchRequest, RequestOptions.DEFAULT));
        ParsedHistogram agg = searchResponse.getAggregations().get(bucket);
        Map map = new LinkedHashMap();
        for (Histogram.Bucket entry : agg.getBuckets()) {
            if (AggsType.count == aggsType) {
                ValueCount count = entry.getAggregations().get(name);
                long value = count.getValue();
                map.put(entry.getKey(), value);
            } else if (AggsType.min == aggsType) {
                ParsedMin min = entry.getAggregations().get(name);
                double value = min.getValue();
                map.put(entry.getKey(), value);
            } else if (AggsType.max == aggsType) {
                ParsedMax max = entry.getAggregations().get(name);
                double value = max.getValue();
                map.put(entry.getKey(), value);
            } else if (AggsType.sum == aggsType) {
                ParsedSum sum = entry.getAggregations().get(name);
                double value = sum.getValue();
                map.put(entry.getKey(), value);
            } else if (AggsType.avg == aggsType) {
                ParsedAvg avg = entry.getAggregations().get(name);
                double value = avg.getValue();
                map.put(entry.getKey(), value);
            }
        }
        return map;

    }

    @Override
    public <T> Map dateHistogramAggs(String metricsName, String routing, AggsType aggsType, Class<T> clazz, String bucketName, int interval, DateIntervalUnit dateIntervalUnit, String... indexes) {
        ElasticsearchPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(clazz);
        String metricsAliasName = entity.getPersistentPropertyWithFieldName(metricsName).getFieldName();
        String bucketAliasName = entity.getPersistentPropertyWithFieldName(bucketName).getFieldName();
        // the name of aggregation
        String bucket = "by_" + bucketAliasName;
        StringJoiner stringJoiner = new StringJoiner("_");
        stringJoiner.add(aggsType.toString());
        stringJoiner.add(metricsAliasName);
        // the name of aggregation
        String name = stringJoiner.toString();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        Function<Integer, DateHistogramInterval> function = DateHistogramIntervalUtil.dateHistogramInterval.get(dateIntervalUnit);

        DateHistogramInterval dateHistogramInterval = function.apply(interval);
        AggregationBuilder aggregation = AggregationBuilders.dateHistogram(bucket).field(bucketAliasName).fixedInterval(dateHistogramInterval);
        searchSourceBuilder.size(0);
        BiFunction<String, String, ValuesSourceAggregationBuilder> result = DateHistogramIntervalUtil.map.get(AggsType.count);
        if (result != null) {
            searchSourceBuilder.aggregation(result.apply(name, metricsAliasName));
        }
        searchSourceBuilder.aggregation(aggregation);
        SearchRequest searchRequest = new SearchRequest();
        if (StringUtils.isNotEmpty(routing)) {
            searchRequest.routing(routing);
        }
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = operations.execute(client -> client.search(searchRequest, RequestOptions.DEFAULT));
        ParsedHistogram agg = searchResponse.getAggregations().get(bucket);
        Map map = new LinkedHashMap();
        for (Histogram.Bucket entry : agg.getBuckets()) {
            if (AggsType.count == aggsType) {
                ValueCount count = entry.getAggregations().get(name);
                long value = count.getValue();
                map.put(entry.getKey(), value);
            } else if (AggsType.min == aggsType) {
                ParsedMin min = entry.getAggregations().get(name);
                double value = min.getValue();
                map.put(entry.getKey(), value);
            } else if (AggsType.max == aggsType) {
                ParsedMax max = entry.getAggregations().get(name);
                double value = max.getValue();
                map.put(entry.getKey(), value);
            } else if (AggsType.sum == aggsType) {
                ParsedSum sum = entry.getAggregations().get(name);
                double value = sum.getValue();
                map.put(entry.getKey(), value);
            } else if (AggsType.avg == aggsType) {
                ParsedAvg avg = entry.getAggregations().get(name);
                double value = avg.getValue();
                map.put(entry.getKey(), value);
            }
        }
        return map;
    }

    @Override
    public <T> long cardinality(String metricName, String routing, long precisionThreshold, Class<T> clazz, String... indexes) {
        String[] indexNames = indexes;
        ElasticsearchPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(clazz);
        String metricAliasName = entity.getPersistentPropertyWithFieldName(metricName).getFieldName();
        String name = "cardinality_" + metricAliasName;
        CardinalityAggregationBuilder aggregation = AggregationBuilders.cardinality(name)
                .field(metricAliasName)
                .precisionThreshold(precisionThreshold);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(0);
        searchSourceBuilder.aggregation(aggregation);
        SearchRequest searchRequest = new SearchRequest(indexNames);
        if (StringUtils.isNotEmpty(routing)) {
            searchRequest.routing(routing);
        }
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = operations.execute(client -> client.search(searchRequest, RequestOptions.DEFAULT));
        Cardinality cardinality = searchResponse.getAggregations().get(name);

        return cardinality.getValue();
    }

    @Override
    public <T> Map multilayerAggs(AggregateBuilder aggregateBuilder, Class<T> clazz) {

        ElasticsearchPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(clazz);
        String indexName = entity.getIndexName();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        String aliasName = entity.getRequiredPersistentProperty(aggregateBuilder.getAggregateName()).getFieldName();
        String name = aggregateBuilder.getAggregateType().toString() + "_" + aliasName;
        TermsAggregationBuilder aggregationBuilder = AggregationBuilders.terms(name).field(aliasName)
                .collectMode(Aggregator.SubAggCollectionMode.BREADTH_FIRST)
                .size(aggregateBuilder.getSize());
        /**
         * 解析`子聚合`.
         * */
        aggregateBuilder.getSubAggregations().stream().forEach(subAggregation -> {
            String subAliasName = null;
            if (!subAggregation.getAggregateName().equals("bucket_sort")) {
                subAliasName = entity.getRequiredPersistentProperty(subAggregation.getAggregateName()).getFieldName();
            }
            String subName = subAggregation.getAggregateType().toString() + "_" + subAliasName;

            /**
             * 如果`子聚合`是term聚合的话则特殊处理，反之亦反.
             * */
            if (subAggregation.getAggregateType().equals(AggsType.terms)) {
                TermsAggregationBuilder subaggregationBuilder = AggregationBuilders.terms(subName).field(subAliasName).size(subAggregation.getSize());
                if (!CollectionUtils.isEmpty(subAggregation.getSubAggregations())) {
                    /**
                     * 解析`子聚合的子聚合`.
                     * */
                    subAggregation.getSubAggregations().stream().forEach(thirdAggregation -> {
                        String thirdAliasName = entity.getRequiredPersistentProperty(thirdAggregation.getAggregateName()).getFieldName();
                        String thirdName = thirdAggregation.getAggregateType().toString() + "_" + thirdAliasName;
                        /**
                         * 如果`子聚合的子聚合`是term聚合的话则特殊处理，反之亦反.
                         * */
                        if (thirdAggregation.getAggregateType().equals(AggsType.terms)) {
                            TermsAggregationBuilder thirdAggregationBuilder = AggregationBuilders.terms(thirdName).field(thirdAliasName).size(10);
                            if (!CollectionUtils.isEmpty(thirdAggregation.getSubAggregations())) {
                                /**
                                 * 解析`子聚合的子聚合的子聚合`.
                                 * */
                                thirdAggregation.getSubAggregations().stream().forEach(fourthAggregation -> {
                                    String fourthAliasName = entity.getRequiredPersistentProperty(fourthAggregation.getAggregateName()).getFieldName();
                                    String fourthName = fourthAggregation.getAggregateType().toString() + "_" + fourthAliasName;
                                    BiFunction<String, String, ValuesSourceAggregationBuilder> fourBiFunction = DateHistogramIntervalUtil.map.get(fourthAggregation.getAggregateType());
                                    thirdAggregationBuilder.subAggregation(fourBiFunction.apply(fourthName, fourthAliasName));
                                });
                            }

                            /**
                             * 处理`子聚合的子聚合`不是term聚合的聚合.
                             * */
                        } else {
                            BiFunction<String, String, ValuesSourceAggregationBuilder> builderBiFunction = DateHistogramIntervalUtil.map.get(thirdAggregation.getAggregateType());
                            if (builderBiFunction != null) {
                                subaggregationBuilder.subAggregation(builderBiFunction.apply(thirdName, thirdAliasName));
                            }
                        }

                    });
                }
                aggregationBuilder.subAggregation(subaggregationBuilder);

                /**
                 * 处理`子聚合`不是term聚合的聚合.
                 * */
            } else {
                /**
                 * 当`子聚合`不是term聚合，而是bucket_sort聚合时则做特殊处理.
                 * */
                if (subAggregation.getAggregateType().equals(AggsType.bucket_sort)) {
                    // todo 根据不同的聚合结果进行排序 现默认根据计算的总金额逆序
                    FieldSortBuilder fieldSortBuilder = SortBuilders.fieldSort("sum_transaction_money").order(SortOrder.DESC);
                    List<FieldSortBuilder> fieldSortBuilders = new ArrayList<>();
                    fieldSortBuilders.add(fieldSortBuilder);
                    aggregationBuilder.subAggregation(new BucketSortPipelineAggregationBuilder("bucket_sort", fieldSortBuilders)
                            .from(subAggregation.getFrom())
                            .size(subAggregation.getSize()));
                    /**
                     * 当`子聚合`既不是term聚合，也不是bucket_sort聚合时.
                     * */
                } else {
                    BiFunction<String, String, ValuesSourceAggregationBuilder> builderBiFunction = DateHistogramIntervalUtil.map.get(subAggregation.getAggregateType());
                    if (builderBiFunction != null) {
                        aggregationBuilder.subAggregation(builderBiFunction.apply(subName, subAliasName));
                    }
                }

            }

        });
        QueryParams params = aggregateBuilder.getQueryParams();
        /**
         * 当查询为查询字段的值存在时，构建term查询.
         * */
        if (StringUtils.isNotEmpty(params.getField())) {
            if (params.getQueryType().equals(QueryType.term)) {
                TermsQueryBuilder termsQueryBuilder = QueryBuilders.termsQuery(params.getField(), params.getValue());
                searchSourceBuilder.query(termsQueryBuilder);
            }
        }

        /**
         * 当查询字段的值不存在时,而模糊查询的值(即keyword)存在时构建regex查询.
         * */
        if (StringUtils.isNotEmpty(params.getField()) && StringUtils.isEmpty(params.getValue()) && StringUtils.isEmpty(params.getField())) {
            searchSourceBuilder.query(builderQuery(params.getValue()));
        }
        searchSourceBuilder.size(0);
        searchSourceBuilder.aggregation(aggregationBuilder);
        SearchRequest searchRequest = new SearchRequest(indexName);

        /**
         * 指定路由查询
         * */
        if (StringUtils.isNotEmpty(aggregateBuilder.getRouting())) {
            searchRequest.routing(aggregateBuilder.getRouting());
        }
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = operations.execute(client -> client.search(searchRequest, RequestOptions.DEFAULT));
        return ParseAggregationResultUtil.parse(searchResponse, name);
    }

    @Override
    public <T> Map histogramAggs(String field, String routing, Double intervel, Double min, Double max, Class<T> clazz) {
        ElasticsearchPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(clazz);
        String indexName = entity.getIndexName();
        String bucketAliasName = entity.getRequiredPersistentProperty(field).getFieldName();
        // the name of aggregation
        String bucket = "histogram_" + bucketAliasName;

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        AggregationBuilder aggregation = AggregationBuilders.histogram(bucket).field(bucketAliasName).interval(intervel)

                .extendedBounds(min, max);

        searchSourceBuilder.size(0);
        searchSourceBuilder.aggregation(aggregation);
        SearchRequest searchRequest = new SearchRequest(indexName);
        if (StringUtils.isNotEmpty(routing)) {
            searchRequest.routing(routing);
        }
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = operations.execute(client -> client.search(searchRequest, RequestOptions.DEFAULT));
        ParsedHistogram agg = searchResponse.getAggregations().get(bucket);
        Map map = new LinkedHashMap();
        for (Histogram.Bucket entry : agg.getBuckets()) {
            map.put(entry.getKey(), entry.getDocCount());
        }
        return map;
    }

    @Override
    public <T> Map rangeAggs(QuerySpecialParams query, String field, String routing, List<Range> ranges, Class<T> clazz) {

        // 构建查询实例
        Object queryTarget = null;
        if (null != query) {

            queryTarget = QueryMappingBuilder.buildDslQueryBuilderMapping(query);
            if (null == queryTarget) {
                log.error("could not build this query instance");
                throw new ElasticsearchException("could not build this query,please check  build method  QueryMappingBuilder.buildDslQueryBuilderMapping() ");
            }
        }

        ElasticsearchPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(clazz);
        String indexName = entity.getIndexName();
        String bucketAliasName = entity.getRequiredPersistentProperty(field).getFieldName();
        // the name of aggregation
        String bucket = "range" + bucketAliasName;

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        AggregationBuilder aggregation = AggregationBuilders.range(bucket).field(bucketAliasName);
        ranges.stream().forEach(range -> {
            ((RangeAggregationBuilder) aggregation).addRange(range.getFrom(), range.getTo());

        });

        searchSourceBuilder.size(0);
        searchSourceBuilder.aggregation(aggregation);
        SearchRequest searchRequest = new SearchRequest(indexName);
        if (StringUtils.isNotEmpty(routing)) {
            searchRequest.routing(routing);
        }
        if (null != queryTarget) {
            searchSourceBuilder.query((QueryBuilder) queryTarget);
        }
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = operations.execute(client -> client.search(searchRequest, RequestOptions.DEFAULT));
        ParsedRange agg = searchResponse.getAggregations().get(bucket);
        Map map = new LinkedHashMap();
        for (org.elasticsearch.search.aggregations.bucket.range.Range.Bucket entry : agg.getBuckets()) {
            map.put(entry.getKey(), entry.getDocCount());
        }
        return map;
    }

    @Override
    public <T> Map statsAggs(QuerySpecialParams query, String field, String routing, Class<T> clazz) {

        // 构建查询实例
        Object queryTarget = null;
        if (null != query) {

            queryTarget = QueryMappingBuilder.buildDslQueryBuilderMapping(query);
            if (null == queryTarget) {
                log.error("could not build this query instance");
                throw new ElasticsearchException("could not build this query,please check  build method  QueryMappingBuilder.buildDslQueryBuilderMapping() ");
            }
        }

        ElasticsearchPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(clazz);
        String indexName = entity.getIndexName();

        String bucketAliasName = entity.getRequiredPersistentProperty(field).getFieldName();
        // the name of aggregation
        String bucket = "stats" + bucketAliasName;
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        AggregationBuilder aggregation = AggregationBuilders.stats(bucket).field(bucketAliasName);
        searchSourceBuilder.size(0);
        searchSourceBuilder.aggregation(aggregation);
        SearchRequest searchRequest = new SearchRequest(indexName);
        if (null != queryTarget) {
            searchSourceBuilder.query((QueryBuilder) queryTarget);
        }
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = operations.execute(client -> client.search(searchRequest, RequestOptions.DEFAULT));
        org.elasticsearch.search.aggregations.metrics.ParsedStats agg = searchResponse.getAggregations().get(bucket);
        Map<String, ParsedStats> result = new HashMap<>();
        result.put("stats", ParsedStats.build(agg));
        return result;
    }

    @Override
    public <T> Map dateHistogramAggs(DateHistogramBuilder dateHistogramBuilder, String routing, Class<T> clazz) {
        ElasticsearchPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(clazz);
        String indexName = entity.getIndexName();
        String bucketAliasName = entity.getRequiredPersistentProperty(dateHistogramBuilder.getField()).getFieldName();
        // the name of aggregation
        String bucket = "date_histogram" + bucketAliasName;

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        DateHistogramAggregationBuilder dateHistogramAggregationBuilder = new DateHistogramAggregationBuilder(bucket);
        dateHistogramAggregationBuilder.field(bucketAliasName);
        dateHistogramAggregationBuilder.calendarInterval(new DateHistogramInterval(dateHistogramBuilder.getDateIntervalUnit()));
        dateHistogramAggregationBuilder.format(dateHistogramBuilder.getFormat());
        dateHistogramAggregationBuilder.minDocCount(dateHistogramBuilder.getMinDocCount());
        dateHistogramBuilder.getChildDateHistogramBuilders().stream().forEach(child -> {
            String aliasName = entity.getRequiredPersistentProperty(child.getField()).getFieldName();
            String name = child.getAggsType() + "_" + aliasName;
            BiFunction<String, String, ValuesSourceAggregationBuilder> result = DateHistogramIntervalUtil.map.get(AggsType.count);
            if (result != null) {
                dateHistogramAggregationBuilder.subAggregation(result.apply(name, aliasName));
            }

        });

        searchSourceBuilder.size(0);
        searchSourceBuilder.aggregation(dateHistogramAggregationBuilder);
        SearchRequest searchRequest = new SearchRequest(indexName);
        if (StringUtils.isNotEmpty(routing)) {
            searchRequest.routing(routing);
        }
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = operations.execute(client -> client.search(searchRequest, RequestOptions.DEFAULT));
        ParsedDateHistogram agg = searchResponse.getAggregations().get(bucket);
        Map map = new LinkedHashMap();
        for (Histogram.Bucket entry : agg.getBuckets()) {
            map.put(entry.getKey(), entry.getDocCount());
        }
        return map;
    }

    @Override
    public <T> Map<String, Object> dateGroupAndSum(QuerySpecialParams params, String dateField, DateSpecificFormat specificFormat,
                                                   String sumField, Class<T> clazz, String routing) {

        // 构建查询实例
        Object queryTarget = null;
        if (null != params) {

            queryTarget = QueryMappingBuilder.buildDslQueryBuilderMapping(params);
            if (null == queryTarget) {
                log.error("could not build this query instance");
                throw new ElasticsearchException("could not build this query,please check  build method  QueryMappingBuilder.buildDslQueryBuilderMapping() ");
            }
        }

        // 构建聚合参数
        AggregationParams root =
                AggregateRequestFactory.createDateGroupAndSum(dateField, specificFormat, sumField);
        // 构建聚合实例
        Object target = AggregationMappingBuilder.buildAggregation(root);

        if (null == target) {
            log.error("could not build this aggregation instance");
            throw new ElasticsearchException("could not build this aggregation,please check  build method  AggregationMappingBuilder.buildAggregationInstance() ");
        }
        // 构建搜索请求
        SearchRequest searchRequest = new SearchRequest(getIndexCoordinates(clazz));
        if (StringUtils.isNotBlank(routing)) {
            searchRequest.routing(routing);
        }
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.size(10);
        // 聚合查询
        sourceBuilder.aggregation((AggregationBuilder) target);
        // 普通查询
        if (null != queryTarget) {
            sourceBuilder.query((QueryBuilder) queryTarget);
        }
        searchRequest.source(sourceBuilder);
        SearchResponse response = operations.execute(client -> client.search(searchRequest, RequestOptions.DEFAULT));
        // TODO 需要将response 解析成map, 方便根据聚合名称取出想要的结果
        Map<String, Object> result = AggregationParser.parseDateGroupAndSum(response.getAggregations());

        return result;
    }

    public <T> Map<String, List<List<Object>>> compoundQueryAndAgg(QuerySpecialParams query, AggregationParams agg, Class<T> clazz, String routing) {

        // 构建查询对象
        Object queryTarget = QueryMappingBuilder.buildDslQueryBuilderMapping(query);

        // 主聚合对象聚合
        Object aggTarget = AggregationMappingBuilder.buildAggregation(agg);

        // 构建兄弟聚合对象
        List<Object> siblingTargets = new ArrayList<>();

        // 兄弟聚合
        if (!CollectionUtils.isEmpty(agg.getSiblingAggregation())) {

            for (AggregationParams aggregationParams : agg.getSiblingAggregation()) {

                Object siblingTarget = AggregationMappingBuilder.buildAggregation(aggregationParams);
                if (null != siblingTarget) {
                    siblingTargets.add(siblingTarget);
                }
            }
        }

        // 构建搜索请求
        SearchRequest searchRequest = new SearchRequest(getIndexCoordinates(clazz));
        if (StringUtils.isNotBlank(routing)) {
            searchRequest.routing(routing);
        }
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // 设置默认参数
        if (null != query) {

            // 设置分页参数
            if (null != query.getPagination()) {

                Pagination pagination = query.getPagination();
                sourceBuilder.from(pagination.getFrom());
                sourceBuilder.size(pagination.getSize());
            }
        }
        // 聚合查询
        if (null != aggTarget) {
            sourceBuilder.aggregation((AggregationBuilder) aggTarget);
            // 是否需要设置兄弟聚合
            if (!CollectionUtils.isEmpty(siblingTargets)) {
                for (Object siblingTarget : siblingTargets) {
                    // 设置兄弟聚合对象
                    sourceBuilder.aggregation((AggregationBuilder) siblingTarget);
                }
            }
        }
        // 普通查询
        if (null != queryTarget) {
            sourceBuilder.query((QueryBuilder) queryTarget);
            // 聚合查询不需要 带出具体数据
            sourceBuilder.size(0);
        }
        searchRequest.source(sourceBuilder);
        SearchResponse response = operations.execute(client -> client.search(searchRequest, RequestOptions.DEFAULT));
        Map<String, List<List<Object>>> result = new HashMap<>();
        // 第一层数据处理
        List<List<Object>> main = AggregationParser.parseMulti(response.getAggregations(), agg.getMapping());
        result.put(agg.getResultName(), main);
        // 同级数据处理
        if (!CollectionUtils.isEmpty(agg.getSiblingAggregation())) {
            for (AggregationParams aggregationParams : agg.getSiblingAggregation()) {
                if (!CollectionUtils.isEmpty(aggregationParams.getMapping())) {
                    List<List<Object>> siblingAggs = AggregationParser.parseMulti(response.getAggregations(), aggregationParams.getMapping());
                    if (!CollectionUtils.isEmpty(siblingAggs)) {
                        result.put(aggregationParams.getResultName(), siblingAggs);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public <T> Page<T> compoundQueryWithoutAgg(Pageable pageable, QuerySpecialParams querySpecialParams, Class<T> clazz, String routing) {
        ElasticsearchPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(clazz);
        String indexName = entity.getIndexName();
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // 构建搜索请求
        SearchRequest searchRequest = new SearchRequest(getIndexCoordinates(clazz));
        // 构建查询对象
        Object queryTarget = QueryMappingBuilder.buildDslQueryBuilderMapping(querySpecialParams);
        // 查询起始值
        int from = querySpecialParams.getPagination().getFrom();
        // 查询的size值
        int size = querySpecialParams.getPagination().getSize();
        // 升序or降序
        String direction = querySpecialParams.getSort().getDirection();
        // 排序字段
        String property = querySpecialParams.getSort().getFieldName();
        // 普通查询
        if (null != queryTarget) {
            sourceBuilder.query((QueryBuilder) queryTarget);
            sourceBuilder.size(size);
            sourceBuilder.from(from);
            sourceBuilder.sort(property, direction.equals("DESC") ? SortOrder.DESC : SortOrder.ASC);
        }
        searchRequest.source(sourceBuilder);
        if (StringUtils.isNotEmpty(routing)) {
            searchRequest.routing(routing);
        }
        PageRequest pageRequest = PageRequest.of(from, size, Sort.by(direction, property));
        SearchHits<T> searchHits = execute(operations -> operations.search(searchRequest, clazz, getIndexCoordinates(clazz)), clazz);
        AggregatedPage<SearchHit<T>> page = SearchHitSupport.page(searchHits, pageRequest);
        return (Page<T>) SearchHitSupport.unwrapSearchHits(page);

    }

    /**
     * @param value: 模糊查询的值
     * @Description: 构建模糊查询参数
     * @return: QueryBuilder
     **/
    private QueryBuilder builderQuery(String value) {

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().should(QueryBuilders.regexpQuery("customer_name", WILDCARD + value + WILDCARD))
                .should(QueryBuilders.regexpQuery("account_card", WILDCARD + value + WILDCARD))
                .should(QueryBuilders.regexpQuery("bank", WILDCARD + value + WILDCARD));

        return boolQueryBuilder;
    }


    @FunctionalInterface
    public interface OperationsCallback<R> {
        @Nullable
        R doWithOperations(ElasticsearchRestTemplate operations);

    }

    @Nullable
    public <R, T> R execute(OperationsCallback<R> callback, Class<T> entityClass) {
        createIndexAndMapping(entityClass);
        return callback.doWithOperations(operations);
    }

    @Nullable
    public <R, T> R executeAndRefresh(OperationsCallback<R> callback, Class<T> entityClass) {
        // 检查entityClass 是否创建了索引
        createIndexAndMapping(entityClass);
        R result = callback.doWithOperations(operations);
        refresh(entityClass);
        return result;
    }
}
