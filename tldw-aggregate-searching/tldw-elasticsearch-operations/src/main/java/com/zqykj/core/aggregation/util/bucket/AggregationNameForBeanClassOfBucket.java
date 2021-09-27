/**
 * @作者 Mcj
 */
package com.zqykj.core.aggregation.util.bucket;

import com.zqykj.core.aggregation.util.AggregationNameForBeanClass;
import org.elasticsearch.search.aggregations.AggregationBuilder;

/**
 * <h1>
 * 描述es 聚合名称 与 聚合类的关系 (针对桶聚合)
 * eg. terms -> TermsAggregationBuilder
 * </h1>
 */
public class AggregationNameForBeanClassOfBucket extends AggregationNameForBeanClass {

    private final static String TYPE = "bucket";
    private final static String packageName = "org.elasticsearch.search.aggregations.bucket";
    private final static Class<?> scanClass = AggregationBuilder.class;

    public AggregationNameForBeanClassOfBucket() {
        getAggregationNameForClassOfType(TYPE, scanClass, packageName);
    }


}
