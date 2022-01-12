/**
 * @作者 Mcj
 */
package com.zqykj.parameters.query;

import com.zqykj.common.enums.QueryType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <h1> 查询的通用参数 </h1>
 */
@Setter
@Getter
@NoArgsConstructor
public class CommonQueryParams {

    /**
     * 查询类型
     */
    private QueryType type;

    /**
     * 要查询的字段
     */
    private String field;

    /**
     * 多字段处理
     */
    private String[] fields;

    /**
     * 字段的值/模糊查询的值
     */
    private Object value;

    /**
     * 脚本参数
     */
    private Script script;

    /**
     * 日期范围
     */
    private DateRange dateRange;

    /**
     * 查询比较符号
     */
    private QueryOperator queryOperator;

    // 内部依然可以是组合查询  (eg. a = 1 and (b = 1 or c = 2) 等等)
    private CombinationQueryParams compoundQueries;

    /**
     * <h2> 脚本参数查询 </h2>
     */
    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Script {

        // 脚本类型: 每种数据源有不同的支持(请参照数据源)
        private String scriptType;

        // 脚本使用语言
        private String lang;

        // 脚本值
        private String idOrCode;

        // 编译参数设置
        private Map<String, String> options;

        // 为脚本执行绑定的用户定义参数
        private Map<String, Object> params;

        public Script(String idOrCode) {
            this.idOrCode = idOrCode;
        }

        public Script(String idOrCode, String scriptType) {
            this.idOrCode = idOrCode;
            this.scriptType = scriptType;
        }

        public Script(String idOrCode, Map<String, Object> params) {
            this.idOrCode = idOrCode;
            this.params = params;
        }

        public Script(String idOrCode, Map<String, Object> params, Map<String, String> options) {
            this.idOrCode = idOrCode;
            this.params = params;
            this.options = options;
        }
    }

    public CommonQueryParams(QueryType type, Script script) {

        this.type = type;
        this.script = script;
    }

    // 嵌套组合查询
    public CommonQueryParams(CombinationQueryParams compoundQueries) {

        this.compoundQueries = compoundQueries;
    }

    public CommonQueryParams(QueryType type, Object value, String... fields) {

        this.type = type;
        this.value = value;
        this.fields = fields;
    }

    public CommonQueryParams(QueryType type, String field, Object value) {

        this.type = type;
        this.field = field;
        this.value = value;
    }

    public CommonQueryParams(QueryType type, String field) {

        this.type = type;
        this.field = field;
    }

    public CommonQueryParams(QueryType type, String field, String... value) {

        this.type = type;
        this.field = field;
        this.value = value;
    }

    public CommonQueryParams(QueryType type, String field, int... value) {

        this.type = type;
        this.field = field;
        this.value = value;
    }

    public CommonQueryParams(QueryType type, String field, long... value) {

        this.type = type;
        this.field = field;
        this.value = value;
    }

    public CommonQueryParams(QueryType type, String field, float... value) {

        this.type = type;
        this.field = field;
        this.value = value;
    }

    public CommonQueryParams(QueryType type, String field, double... value) {

        this.type = type;
        this.field = field;
        this.value = value;
    }

    public CommonQueryParams(QueryType type, String field, Object... value) {

        this.type = type;
        this.field = field;
        this.value = value;
    }

    public CommonQueryParams(QueryType type, String field, Iterable<?> value) {

        this.type = type;
        this.field = field;
        this.value = value;
    }

    public CommonQueryParams(QueryType type, String field, String value) {

        this.type = type;
        this.field = field;
        this.value = value;
    }

    public CommonQueryParams(QueryType type, String field, Object value, DateRange range) {

        this.type = type;
        this.field = field;
        this.value = value;
        this.dateRange = range;
    }

    public CommonQueryParams(QueryType type, String field, DateRange range) {

        this.type = type;
        this.field = field;
        this.dateRange = range;
    }

    public CommonQueryParams(QueryType type, String field, Object value, DateRange range, QueryOperator operator) {

        this.type = type;
        this.field = field;
        this.value = value;
        this.dateRange = range;
        this.queryOperator = operator;
    }

    public CommonQueryParams(QueryType type, String field, Object value, QueryOperator operator) {

        this.type = type;
        this.field = field;
        this.value = value;
        this.queryOperator = operator;
    }
}
