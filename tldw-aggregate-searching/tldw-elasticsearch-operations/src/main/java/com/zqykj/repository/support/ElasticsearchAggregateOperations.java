package com.zqykj.repository.support;

import com.zqykj.common.request.AggregateBuilder;
import com.zqykj.common.request.DateHistogramParams;
import com.zqykj.common.response.Stats;
import com.zqykj.core.ElasticsearchRestTemplate;
import com.zqykj.core.IndexOperations;
import com.zqykj.core.mapping.ElasticsearchPersistentEntity;
import com.zqykj.core.mapping.ElasticsearchPersistentProperty;
import com.zqykj.domain.Page;
import com.zqykj.domain.Pageable;
import com.zqykj.enums.AggsType;
import com.zqykj.mapping.context.MappingContext;
import com.zqykj.repository.AggregateOpertions;
import com.zqykj.repository.EntranceRepository;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.support.ValuesSourceAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @Description: TODO
 * @Author zhangkehou
 * @Date 2021/9/15
 */
@Slf4j
public class ElasticsearchAggregateOperations implements EntranceRepository {

    private ElasticsearchRestTemplate operations;
    private final MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext;

    private static Map<AggsType, BiFunction<String,String, ValuesSourceAggregationBuilder>>  map = new HashMap<>();
    static{
        map.put(AggsType.count,(type,field) -> AggregationBuilders.count(type).field(field) );
        map.put(AggsType.avg,(type,field) -> AggregationBuilders.avg(type).field(field));
        map.put(AggsType.max,(type,field) -> AggregationBuilders.max(type).field(field));
        map.put(AggsType.min,(type,field) -> AggregationBuilders.min(type).field(field));
        map.put(AggsType.sum,(type,field) -> AggregationBuilders.sum(type).field(field));
    }

    public ElasticsearchAggregateOperations(ElasticsearchRestTemplate operations, MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext){
        this.operations =operations;
        this.mappingContext = mappingContext;
    }


    @Override
    public Object metricsAggs(String metricsName, AggsType aggsType, Class clazz, String... indexes) {
        String[] indexNames = indexes;
        ElasticsearchPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(clazz);
        String aliasName = entity.getPersistentPropertyWithFieldName("name").getFieldName();
        StringJoiner stringJoiner = new StringJoiner("_");
        stringJoiner.add(aggsType.toString());
        stringJoiner.add(aliasName);
        // the name of aggregation
        String name = stringJoiner.toString();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BiFunction<String,String, ValuesSourceAggregationBuilder> result = map.get(AggsType.count);
        if (result != null){
            searchSourceBuilder.aggregation(result.apply(name,aliasName));
        }
        searchSourceBuilder.size(0);
        SearchRequest searchRequest = new SearchRequest(indexNames);

        return null;
    }

    @Override
    public Stats statsAggs(String metricsName, Class clazz, String... indexes) {
        return null;
    }

    @Override
    public Map<String, Stats> statsAggs(String metricsName, Class clazz, String bucketName, String... indexes) {
        String[] indexNames = indexes;
        ElasticsearchPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(clazz);
        entity.getPersistentPropertyWithFieldName("name").getFieldName();
    }

    @Override
    public Map<Double, Double> percentilesAggs(String metricsName, Class clazz, double[] customSegment, String... indexes) {
        return null;
    }

    @Override
    public Map<Double, Double> percentilesRanksAggs(String metricsName, Class clazz, double[] customSegment, String... indexes) {
        return null;
    }

    @Override
    public Map histogramAggs(String metricsName, AggsType aggsType, Class clazz, String bucketName, double interval, String... indexes) {
        return null;
    }

    @Override
    public Map dateHistogramAggs(String metricsName, AggsType aggsType, Class clazz, String bucketName, DateHistogramParams dateHistogramParams, String... indexes) {
        return null;
    }

    @Override
    public long cardinality(String metricName, long precisionThreshold, Class clazz, String indexes) {
        return 0;
    }

    @Override
    public Object save(Object entity, String routing) {
        return null;
    }

    @Override
    public Iterable saveAll(Iterable entities, String routing) {
        return null;
    }

    @Override
    public Optional findById(Object o, String routing) throws Exception {
        return Optional.empty();
    }

    @Override
    public Iterable findAll(String routing) {
        return null;
    }

    @Override
    public Page findAll(Pageable pageable, String routing) {
        return null;
    }

    @Override
    public Iterable findAllById(Iterable iterable, String routing) {
        return null;
    }

    @Override
    public long count(String routing) {
        return 0;
    }

    @Override
    public void deleteById(Object o, String routing) {

    }

    @Override
    public void delete(Object entity, String routing) {

    }

    @Override
    public void deleteAll(Iterable entities, String routing) {

    }

    @Override
    public void deleteAll(String routing) {

    }
}
