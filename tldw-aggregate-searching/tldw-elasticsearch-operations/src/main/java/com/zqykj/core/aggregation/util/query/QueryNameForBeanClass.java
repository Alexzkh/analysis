/**
 * @作者 Mcj
 */
package com.zqykj.core.aggregation.util.query;

import com.zqykj.core.aggregation.util.ClassNameForBeanClass;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilder;


/**
 * <h1>
 * 描述es 查询名称 与 查询类的关系 抽象类
 * </h1>
 */
@Slf4j
public class QueryNameForBeanClass extends ClassNameForBeanClass {

    private final static String TYPE = "dsl";
    private final static String packageName = "org.elasticsearch.index.query";
    private final static Class<?> scanClass = QueryBuilder.class;

    public QueryNameForBeanClass() {
        getAggregationNameForClassOfType(TYPE, scanClass, packageName);
    }
}
