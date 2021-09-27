/**
 * @作者 Mcj
 */
package com.zqykj.core.aggregation.util.pipeline;

import com.zqykj.core.aggregation.util.AggregationNameForBeanClass;
import org.elasticsearch.search.aggregations.PipelineAggregationBuilder;

/**
 * <h1>
 * 描述es 聚合名称 与 聚合类的关系 (针对 指标聚合)
 * eg. bucket_script -> BucketScriptPipelineAggregationBuilder
 * </h1>
 */
public class AggregationNameForBeanClassOfPipeline extends AggregationNameForBeanClass {

    private final static String TYPE = "pipeline";
    private final static String packageName = "org.elasticsearch.search.aggregations.pipeline";
    private final static Class<?> scanClass = PipelineAggregationBuilder.class;

    public AggregationNameForBeanClassOfPipeline() {
        getAggregationNameForClassOfType(TYPE, scanClass, packageName);
    }
}
