/**
 * @作者 Mcj
 */
package com.zqykj.parameters.query;

import com.zqykj.common.enums.QueryType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * <h1> 查询的通用参数 </h1>
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
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
     * 字段的值
     */
    private Object value;
}
