/**
 * @作者 Mcj
 */
package com.zqykj.parameters.query;

import com.zqykj.common.enums.QueryType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

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
     * 字段的值/模糊查询的值
     */
    private Object value;

    /**
     * 日期范围
     */
    private DateRange dateRange;

    /**
     * 查询比较符号
     */
    private QueryOperator queryOperator;

    // 内部依然可以是组合查询  (eg. 需要满足 满足a=1或b=2, 并且c=3)
    private CombinationQueryParams compoundQueries;

    public CommonQueryParams(QueryType type, String field, Object value) {

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
