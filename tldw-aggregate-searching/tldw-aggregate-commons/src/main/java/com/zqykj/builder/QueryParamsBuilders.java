/**
 * @作者 Mcj
 */
package com.zqykj.builder;

import com.zqykj.common.enums.QueryType;
import com.zqykj.parameters.query.CommonQueryParams;

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
}
