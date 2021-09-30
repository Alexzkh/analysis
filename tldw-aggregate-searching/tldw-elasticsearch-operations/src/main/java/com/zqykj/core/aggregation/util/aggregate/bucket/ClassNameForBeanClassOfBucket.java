/**
 * @作者 Mcj
 */
package com.zqykj.core.aggregation.util.aggregate.bucket;

import com.zqykj.core.aggregation.util.ClassNameForBeanClass;
import org.elasticsearch.search.aggregations.AggregationBuilder;

/**
 * <h1>
 * 描述es 聚合名称 与 聚合类的关系 (针对桶聚合)
 * eg. terms -> TermsAggregationBuilder
 * </h1>
 */
public class ClassNameForBeanClassOfBucket extends ClassNameForBeanClass {

    private final static String TYPE = "bucket";
    private final static String packageName = "org.elasticsearch.search.aggregations.bucket";
    private final static Class<?> scanClass = AggregationBuilder.class;

    public ClassNameForBeanClassOfBucket() {
        getAggregationNameForClassOfType(TYPE, scanClass, packageName);
    }


}
