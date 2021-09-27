/**
 * @作者 Mcj
 */
package com.zqykj.core.aggregation.util.metrics;

import com.zqykj.core.aggregation.util.AggregationNameForBeanClass;
import org.elasticsearch.search.aggregations.AggregationBuilder;

/**
 * <h1>
 * 描述es 聚合名称 与 聚合类的关系 (针对 指标聚合)
 * eg. max -> MaxAggregationBuilder
 * </h1>
 */
public class AggregationNameForBeanClassOfMetrics extends AggregationNameForBeanClass {

    private final static String TYPE = "metrics";
    private final static String packageName = "org.elasticsearch.search.aggregations.metrics";
    private final static Class<?> scanClass = AggregationBuilder.class;


    public AggregationNameForBeanClassOfMetrics() {
        getAggregationNameForClassOfType(TYPE, scanClass, packageName);
    }
}
