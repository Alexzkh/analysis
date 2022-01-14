/**
 * @作者 Mcj
 */
package com.zqykj.builder;

import com.zqykj.common.enums.QueryType;
import com.zqykj.parameters.query.CommonQueryParams;
import com.zqykj.parameters.query.DateRange;
import com.zqykj.parameters.query.QueryOperator;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Map;

/**
 * <h1> Elasticsearch 查询参数构建 </h1>
 */
public class QueryParamsBuilders {

    // es 默认脚本类型(其他数据源自行切换)
    public static final String DEFAULT_SCRIPT_TYPE = "INLINE";
    // es 默认脚本语言(其他数据源自行切换)
    public static final String DEFAULT_SCRIPT_LANG = "painless";

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
     * <h2> 单个值,多字段匹配 </h2>
     */
    public static CommonQueryParams multiMatch(String value, String... fields) {

        return new CommonQueryParams(QueryType.multi_match, value, fields);
    }

    /**
     * <h2> 单个值,多字段匹配 </h2>
     */
    public static CommonQueryParams multiMatch(Object value, String... fields) {

        return new CommonQueryParams(QueryType.multi_match, value, fields);
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

    /**
     * <h2> 脚本查询 </h2>
     */
    public static CommonQueryParams script(String idOrCode) {

        return script(DEFAULT_SCRIPT_TYPE, DEFAULT_SCRIPT_LANG, idOrCode, Collections.emptyMap(), Collections.emptyMap());
    }

    public static CommonQueryParams script(String idOrCode, Map<String, Object> params) {

        return script(DEFAULT_SCRIPT_TYPE, DEFAULT_SCRIPT_LANG, idOrCode, Collections.emptyMap(), params);
    }

    public static CommonQueryParams script(String scriptType, String lang, String idOrCode, Map<String, String> options, Map<String, Object> params) {

        if (StringUtils.isBlank(idOrCode)) {
            throw new RuntimeException("Scripting language query value cannot be empty!");
        }
        return new CommonQueryParams(QueryType.script, new CommonQueryParams.Script(scriptType, lang, idOrCode, options, params));
    }
}
