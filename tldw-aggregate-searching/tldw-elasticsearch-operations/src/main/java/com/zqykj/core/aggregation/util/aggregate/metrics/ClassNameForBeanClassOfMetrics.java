/**
 * @作者 Mcj
 */
package com.zqykj.core.aggregation.util.aggregate.metrics;

import com.zqykj.core.aggregation.util.ClassNameForBeanClass;
import org.elasticsearch.search.aggregations.AggregationBuilder;

/**
 * <h1>
 * 描述es 聚合名称 与 聚合类的关系 (针对 指标聚合)
 * eg. max -> MaxAggregationBuilder
 * </h1>
 */
public class ClassNameForBeanClassOfMetrics extends ClassNameForBeanClass {

    private final static String TYPE = "metrics";
    private final static String packageName = "org.elasticsearch.search.aggregations.metrics";
    private final static Class<?> scanClass = AggregationBuilder.class;


    public ClassNameForBeanClassOfMetrics() {
        getAggregationNameForClassOfType(TYPE, scanClass, packageName);
    }
}
