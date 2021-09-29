/**
 * @作者 Mcj
 */
package com.zqykj.parameters.query;

import com.zqykj.parameters.Pagination;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * <h1> 查询参数 </h1>
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class QueryParameters {

    /**
     * 查询类型
     */
    private String type;

    /**
     * 要查询的字段
     */
    private String field;

    /**
     * 字段的值
     */
    private Object value;

    // 查询的一些通用参数设置
    private QueryGeneralParameters generalParameters;

    // 分页参数设置
    private Pagination pagination;

    /**
     * <h1> 查询类型的翻译 </h1>
     * TODO 后面做一个翻译器, 将公共的类型 翻译成不同数据源需要的查询类型
     */
    public String queryTypeConvert(String type) {

        return type;
    }
}
