/**
 * @作者 Mcj
 */
package com.zqykj.builder;

import com.zqykj.common.enums.QueryType;
import com.zqykj.parameters.query.CommonQueryParams;
import com.zqykj.parameters.query.DateRange;
import com.zqykj.parameters.query.QueryOperator;

/**
 * <h1> Elasticsearch 查询参数构建 </h1>
 */
public class QueryParamsBuilders {


    /**
     * <h2> 精准匹配查询 </h2>
     */
    public static CommonQueryParams term(String field, Object value) {

        return new CommonQueryParams(QueryType.term, field, value);
    }

    /**
     * <h2> 类似于in查询 </h2>
     */
    public static CommonQueryParams terms(String field, Iterable<?> value) {

        return new CommonQueryParams(QueryType.terms, field, value);
    }

    /**
     * <h2> 类似于in查询 </h2>
     */
    public static CommonQueryParams terms(String field, String... value) {

        return new CommonQueryParams(QueryType.terms, field, value);
    }

    /**
     * <h2> 日期范围筛选 </h2>
     */
    public static CommonQueryParams range(String field, DateRange dateRange) {

        return new CommonQueryParams(QueryType.range, field, dateRange);
    }

    /**
     * <h2> 数值类范围筛选 </h2>
     */
    public static CommonQueryParams range(String field, Object value, QueryOperator operator) {

        return new CommonQueryParams(QueryType.range, field, value, operator);
    }

    /**
     * <h2> 单个字段,多值匹配 </h2>
     */
    public static CommonQueryParams multiMatch(String field, String... value) {

        return new CommonQueryParams(QueryType.multi_match, field, value);
    }

    /**
     * <h2> 单个字段,多值匹配 </h2>
     */
    public static CommonQueryParams multiMatch(String field, Object... value) {

        return new CommonQueryParams(QueryType.multi_match, field, value);
    }

    /**
     * <h2> 模糊查询 </h2>
     */
    public static CommonQueryParams fuzzy(String field, String value) {

        return new CommonQueryParams(QueryType.wildcard, field, value);
    }

    /**
     * <h2> 过滤null、空值等 </h2>
     */
    public static CommonQueryParams exists(String field) {

        return new CommonQueryParams(QueryType.exists, field);
    }
}
