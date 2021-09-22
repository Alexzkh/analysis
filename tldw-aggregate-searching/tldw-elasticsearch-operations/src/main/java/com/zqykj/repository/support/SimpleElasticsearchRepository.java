/**
 * @作者 Mcj
 */
package com.zqykj.repository.support;

import com.zqykj.common.enums.AggregateType;
import com.zqykj.common.request.AggregateBuilder;
import com.zqykj.common.response.ParsedStats;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.search.aggregations.*;
import org.elasticsearch.search.aggregations.metrics.Stats;
import com.zqykj.core.*;
import com.zqykj.core.aggregation.AggregatedPage;
import com.zqykj.core.mapping.ElasticsearchPersistentEntity;
import com.zqykj.core.mapping.ElasticsearchPersistentProperty;
import com.zqykj.core.query.NativeSearchQueryBuilder;
import com.zqykj.domain.Page;
import com.zqykj.domain.PageImpl;
import com.zqykj.domain.PageRequest;
import com.zqykj.domain.Pageable;
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
import org.elasticsearch.index.query.IdsQueryBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.histogram.ParsedHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.*;
import org.elasticsearch.search.aggregations.support.ValuesSourceAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.idsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;

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

    private static Map<AggsType, BiFunction<String, String, ValuesSourceAggregationBuilder>> map = new ConcurrentHashMap<>();

    static {
        map.put(AggsType.count, (type, field) -> AggregationBuilders.count(type).field(field));
        map.put(AggsType.avg, (type, field) -> AggregationBuilders.avg(type).field(field));
        map.put(AggsType.max, (type, field) -> AggregationBuilders.max(type).field(field));
        map.put(AggsType.min, (type, field) -> AggregationBuilders.min(type).field(field));
        map.put(AggsType.sum, (type, field) -> AggregationBuilders.sum(type).field(field));
        map.put(AggsType.terms, (type, field) -> AggregationBuilders.terms(type).field(field));

    }

    private static Map<DateIntervalUnit, Function<Integer, DateHistogramInterval>> dateHistogramInterval = new ConcurrentHashMap<>();

    static {
        dateHistogramInterval.put(DateIntervalUnit.SECOND, sec -> DateHistogramInterval.seconds(sec));
        dateHistogramInterval.put(DateIntervalUnit.DAY, sec -> DateHistogramInterval.days(sec));
        dateHistogramInterval.put(DateIntervalUnit.MINUTE, minutes -> DateHistogramInterval.minutes(minutes));
        dateHistogramInterval.put(DateIntervalUnit.HOUR, hours -> DateHistogramInterval.hours(hours));
        dateHistogramInterval.put(DateIntervalUnit.WEEK, week -> DateHistogramInterval.weeks(week));
        dateHistogramInterval.put(DateIntervalUnit.QUARTER, quarter -> DateHistogramInterval.QUARTER);
        dateHistogramInterval.put(DateIntervalUnit.MONTH, month -> DateHistogramInterval.MONTH);
        dateHistogramInterval.put(DateIntervalUnit.YEAR, year -> DateHistogramInterval.YEAR);
    }




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

    @SuppressWarnings("unchecked")
    @Override
    public <T> Page<T> findAll(Pageable pageable, String routing, @NonNull Class<T> entityClass) {

        NativeSearchQuery query = new NativeSearchQueryBuilder().withQuery(matchAllQuery()).withPageable(pageable).build();

        SearchHits<T> searchHits = execute(operations -> operations.search(query, entityClass, getIndexCoordinates(entityClass)), entityClass);

        AggregatedPage<SearchHit<T>> page = SearchHitSupport.page(searchHits, query.getPageable());
        return (Page<T>) SearchHitSupport.unwrapSearchHits(page);
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
        BiFunction<String, String, ValuesSourceAggregationBuilder> result = map.get(AggsType.count);
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
        BiFunction<String, String, ValuesSourceAggregationBuilder> result = map.get(AggsType.count);
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
        Function<Integer, DateHistogramInterval> function = dateHistogramInterval.get(dateIntervalUnit);

        DateHistogramInterval dateHistogramInterval = function.apply(interval);
        AggregationBuilder aggregation = AggregationBuilders.dateHistogram(bucket).field(bucketAliasName).fixedInterval(dateHistogramInterval);
        searchSourceBuilder.size(0);
        BiFunction<String, String, ValuesSourceAggregationBuilder> result = map.get(AggsType.count);
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
        aggregateBuilder.getSubAggregations().stream().forEach(subAggregation -> {
            String subAliasName = entity.getRequiredPersistentProperty(subAggregation.getAggregateName()).getFieldName();
            String subName = subAggregation.getAggregateType().toString() + "_" + subAliasName;
            if (subAggregation.getAggregateType().equals(AggregateType.terms)) {
                TermsAggregationBuilder subaggregationBuilder = AggregationBuilders.terms(subName).field(subAliasName).size(subAggregation.getSize());
                if (!CollectionUtils.isEmpty(subAggregation.getSubAggregations())) {
                    subAggregation.getSubAggregations().stream().forEach(thirdAggregation -> {
                        String thirdAliasName = entity.getRequiredPersistentProperty(thirdAggregation.getAggregateName()).getFieldName();
                        String thirdName = thirdAggregation.getAggregateType().toString() + "_" + thirdAliasName;
                        if (thirdAggregation.getAggregateType().equals(AggregateType.terms)) {
                            TermsAggregationBuilder thirdAggregationBuilder = AggregationBuilders.terms(thirdName).field(thirdAliasName);
                            if (!CollectionUtils.isEmpty(thirdAggregation.getSubAggregations())) {
                                thirdAggregation.getSubAggregations().stream().forEach(fourthAggregation -> {
                                    String fourthAliasName = entity.getRequiredPersistentProperty(fourthAggregation.getAggregateName()).getFieldName();
                                    String fourthName = fourthAggregation.getAggregateType().toString() + "_" + fourthAliasName;
                                    BiFunction<String, String, ValuesSourceAggregationBuilder> fourBiFunction = map.get(fourthAggregation.getAggregateType());
                                    thirdAggregationBuilder.subAggregation(fourBiFunction.apply(fourthName, fourthAliasName));
                                });
                            }
                        }
                        BiFunction<String, String, ValuesSourceAggregationBuilder> builderBiFunction = map.get(thirdAggregation.getAggregateType());
                        if (builderBiFunction != null) {
                            subaggregationBuilder.subAggregation(builderBiFunction.apply(thirdName, thirdAliasName));
                        }
                    });
                }
                aggregationBuilder.subAggregation(subaggregationBuilder);
            }
            BiFunction<String, String, ValuesSourceAggregationBuilder> builderBiFunction = map.get(subAggregation.getAggregateType());
            if (builderBiFunction != null) {
                aggregationBuilder.subAggregation(builderBiFunction.apply(subName, subAliasName));
            }
        });
        searchSourceBuilder.size(0);
        searchSourceBuilder.aggregation(aggregationBuilder);
        SearchRequest searchRequest = new SearchRequest(indexName);
        if (StringUtils.isNotEmpty(aggregateBuilder.getRouting())) {
            searchRequest.routing(aggregateBuilder.getRouting());
        }
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = operations.execute(client -> client.search(searchRequest, RequestOptions.DEFAULT));
        Aggregations aggregations = searchResponse.getAggregations();
        return aggregations.asMap();
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
