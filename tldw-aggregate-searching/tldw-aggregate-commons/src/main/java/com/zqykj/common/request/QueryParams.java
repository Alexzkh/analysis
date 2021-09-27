package com.zqykj.common.request;

import com.zqykj.common.enums.QueryType;
import com.zqykj.domain.Pageable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 聚合之外的查询参数
 * @Author zhangkehou
 * @Date 2021/9/24
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QueryParams {

    /**
     * 查询类型
     */
    private QueryType queryType;

    /**
     * 字段
     */
    private String field;

    /**
     * 精准匹配的值
     **/
    private String value;

    /**
     * 查询起始值
     */
    private int from;

    /**
     * 查询的个数
     */
    private int size;

}
