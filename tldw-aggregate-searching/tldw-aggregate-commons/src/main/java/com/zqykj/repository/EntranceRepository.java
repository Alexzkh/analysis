/**
 * @作者 Mcj
 */
package com.zqykj.repository;

import com.zqykj.common.request.AggregateBuilder;
import com.zqykj.common.request.DateHistogramBuilder;
import com.zqykj.common.response.ParsedStats;
import com.zqykj.domain.Page;
import com.zqykj.domain.Pageable;
import com.zqykj.domain.Range;
import com.zqykj.enums.AggsType;
import com.zqykj.enums.DateIntervalUnit;

import java.util.List;
import java.util.Map;

import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.query.QuerySpecialParams;
import org.springframework.context.annotation.Primary;

/**
 * <h1> 提供给外部使用的公共入口Repository </h1>
 */
@Primary
public interface EntranceRepository extends CrudRepository {

    /**
     * @param metricsName: the field of aggrating ,the same as domain field name ;
     * @param aggsType:    aggregation type
     * @param routing:     the shard of routing
     * @param clazz:       domain type
     * @param indexes:the  index name collection
     * @description : Numeric metrics aggregations are a special type of metrics aggregation which output numeric values.
     * calculate the corresponding statistical value according to the type.
     * @return: aggregation result
     **/
    <T> Double metricsAggs(String metricsName, AggsType aggsType, String routing, Class<T> clazz, String... indexes);

    /**
     * @param metricsName: the field of aggrating ,the same as domain field name ;
     * @param routing:     the shard of routing
     * @param clazz:       domain type
     * @param indexes:the  index name collection
     * @desription: A multi-value metrics aggregation that computes stats over numeric values extracted from the aggregated documents.
     * These values can be extracted either from specific numeric fields in the documents,or be generated by a provided script.
     * The stats that are returned consist of: min, max, sum, count and avg.
     * @return: com.zqykj.common.response.Stats
     **/
    <T> ParsedStats statsAggs(String metricsName, String routing, Class<T> clazz, String... indexes);

    /**
     * @param metricsName: the field of aggrating ,the same as domain field name ;
     * @param routing:     the shard of routing
     * @param clazz:domain type
     * @param bucketName:
     * @param indexes:the  index name collection
     * @desription: A multi-value metrics aggregation that computes stats over numeric values extracted from the aggregated documents.
     * These values can be extracted either from specific numeric fields in the documents,or be generated by a provided script.
     * The stats that are returned consist of: min, max, sum, count and avg.
     * @return: java.util.Map<java.lang.String, com.zqykj.common.response.Stats>
     **/
    <T> Map<String, ParsedStats> statsAggs(String metricsName, String routing, Class<T> clazz, String bucketName, String... indexes);

    /**
     * @param metricsName:   the field of aggrating ,the same as domain field name ;
     * @param routing:       the shard of routing
     * @param clazz:domain   type
     * @param customSegment: custom segment
     * @param indexes:the    index name collection
     * @description: A multi-value metrics aggregation that calculates one or more percentiles over numeric values extracted from the aggregated documents.
     * These values can be generated by a provided script or extracted from specific numeric or histogram fields in the documents.
     * Percentiles show the point at which a certain percentage of observed values occur.
     * For example, the 95th percentile is the value which is greater than 95% of the observed values.
     * @return: java.util.Map<java.lang.Double, java.lang.Double>
     **/
    <T> Map<Double, Double> percentilesAggs(String metricsName, String routing, Class<T> clazz, double[] customSegment, String... indexes);

    /**
     * @param metricsName:   the field of aggrating ,the same as domain field name ;
     * @param routing:       the shard of routing
     * @param clazz:domain   type
     * @param customSegment: Custom percentage segment
     * @param indexes:the    index name collection
     * @description : A multi-value metrics aggregation that calculates one or more percentile ranks over numeric values extracted from the aggregated documents.
     * These values can be generated by a provided script or extracted from specific numeric or histogram fields in the documents.
     * Percentile rank show the percentage of observed values which are below certain value.
     * For example, if a value is greater than or equal to 95% of the observed values it is said to be at the 95th percentile rank.
     * @return: java.util.Map<java.lang.Double, java.lang.Double>
     **/
    <T> Map<Double, Double> percentilesRanksAggs(String metricsName, String routing, Class<T> clazz, double[] customSegment, String... indexes);

    /**
     * @param metricsName: the field of aggrating ,the same as domain field name ;
     * @param routing:     the shard of routing
     * @param aggsType:    aggregation type
     * @param clazz:domain type
     * @param bucketName:  bucket aggregation Name
     * @param interval:    the histogram interval
     * @param indexes:the  index name collection
     * @description: A multi-bucket values source based aggregation that can be applied on numeric values or numeric range values extracted from the documents.
     * It dynamically builds fixed size (a.k.a. interval) buckets over the values.
     * @return: java.util.Map
     **/
    <T> Map histogramAggs(String metricsName, String routing, AggsType aggsType, Class<T> clazz, String bucketName, double interval, String... indexes);

    /**
     * @param metricsName:          the field of aggrating ,the same as domain field name ;
     * @param routing:              the shard of routing
     * @param aggsType:             aggregation type
     * @param clazz:                domain type
     * @param bucketName:           bucket aggregation Name
     * @param interval:the          histogram interval
     * @param dateIntervalUnit:date type
     * @param indexes:              the index name collection
     * @description: This multi-bucket aggregation is similar to the normal histogram,but it can only be used with date or date range values.
     * Because dates are represented internally in Elasticsearch as long values,
     * it is possible, but not as accurate, to use the normal histogram on dates as well.
     * The main difference in the two APIs is that here the interval can be specified using date/time expressions.
     * Time-based data requires special support because time-based intervals are not always a fixed length.
     * @return: java.util.Map
     **/
    <T> Map dateHistogramAggs(String metricsName, String routing, AggsType aggsType, Class<T> clazz, String bucketName, int interval, DateIntervalUnit dateIntervalUnit, String... indexes);

    /**
     * @param metricName:         the field of aggrating ,the same as domain field name ;
     * @param routing:            the shard of routing
     * @param precisionThreshold: precision threshold
     * @param clazz:domain        type
     * @param indexes:the         index name collection
     * @description: A single-value metrics aggregation that calculates an approximate count of distinct values.
     * Values can be extracted either from specific fields in the document or generated by a script.
     * @return: long
     **/
    <T> long cardinality(String metricName, String routing, long precisionThreshold, Class<T> clazz, String... indexes);

    /**
     * @param aggregateBuilder: aggregate builder for multilayer aggs
     * @param clazz:            domain type
     * @description: Supports four levels of aggregation
     * @return: java.util.Map
     **/
    <T> Map multilayerAggs(AggregateBuilder aggregateBuilder, Class<T> clazz);


    /**
     * A multi-bucket values source based aggregation that can
     * be applied on numeric values or numeric range values extracted from the documents.
     * It dynamically builds fixed size (a.k.a. interval) buckets over the values.
     *
     * @param field:    the field of aggrating ,the same as domain field name.
     * @param routing:  the shard of routing.
     * @param intervel: the histogram interval.
     * @param min:      minimum
     * @param max:      maximum
     * @param clazz:    the domain type
     * @return: java.util.Map
     **/
    <T> Map histogramAggs(String field, String routing, Double intervel, Double min, Double max, Class<T> clazz);


    /**
     * A multi-bucket value source based aggregation that enables the user to define a set of ranges
     * - each representing a bucket. During the aggregation process,
     * the values extracted from each document will be checked against
     * each bucket range and "bucket" the relevant/matching document.
     * Note that this aggregation includes the from value and excludes the to value for each range.
     *
     * @param query:   the query parameter.
     * @param field:   the field of aggrating ,the same as domain field name .
     * @param routing: the shard of routing.
     * @param ranges:  range aggregation parameter.
     * @param clazz:   the domain type .
     * @return: java.util.Map
     **/
    <T> Map rangeAggs(QuerySpecialParams query, String field, String routing, List<Range> ranges, Class<T> clazz);


    /**
     * A multi-bucket value source based aggregation that enables the user to define a set of ranges
     * - each representing a bucket. During the aggregation process,
     * the values extracted from each document will be checked against
     * each bucket range and "bucket" the relevant/matching document.
     * Note that this aggregation includes the from value and excludes the to value for each range.
     *
     * @param query:   the query parameter.
     * @param field:   the field of aggrating ,the same as domain field name .
     * @param routing: the shard of routing.
     * @param clazz:   the domain type .
     * @return: java.util.Map
     **/
    <T> Map statsAggs(QuerySpecialParams query, String field, String routing, Class<T> clazz);

    /**
     * This multi-bucket aggregation is similar to the normal histogram,
     * but it can only be used with date or date range values.
     * Because dates are represented internally in Elasticsearch as long values,
     * it is possible, but not as accurate, to use the normal histogram on dates as well.
     * The main difference in the two APIs is that here the interval can be specified using date/time expressions.
     * Time-based data requires special support because time-based intervals are not always a fixed length
     *
     * @param dateHistogramBuilder: the date histogram aggregation parameter .
     * @param routing:              the shard of routing.
     * @param clazz:                the domain type .
     * @return: java.util.Map
     **/
    <T> Map dateHistogramAggs(DateHistogramBuilder dateHistogramBuilder, String routing, Class<T> clazz);

    /**
     * <h2> 按日期间隔分组并根据某个字段进行汇总求和 </h2>
     *
     * @param query   查询参数 (eg. 可以先筛选数据,在对数据进行聚合统计分析)
     * @param dateAgg 日期聚合参数
     * @param clazz   实体类
     * @param routing 路由
     */
    <T> List<List<Object>> dateGroupAgg(QuerySpecialParams query, AggregationParams dateAgg, Class<T> clazz, String routing);

    /**
     * <h2> 组合查询与聚合 (只返回聚合结果) </h2>
     *
     * @param query   查询参数
     * @param agg     聚合参数
     * @param clazz   实体类
     * @param routing 路由
     */
    <T> Map<String, List<List<Object>>> compoundQueryAndAgg(QuerySpecialParams query, AggregationParams agg, Class<T> clazz, String routing);

    /**
     * <h2>组合查询（只返回查询结果）</h2>
     *
     * @param querySpecialParams: 查询参数
     * @param clazz:              实体类
     * @param routing:            路由
     * @return: java.util.List<T>
     **/
    <T> Page<T> compoundQueryWithoutAgg(Pageable pageable, QuerySpecialParams querySpecialParams, Class<T> clazz, String routing);
}
