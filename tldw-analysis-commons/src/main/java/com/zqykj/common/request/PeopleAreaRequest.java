package com.zqykj.common.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 人员地域请求体
 * @Author zhangkehou
 * @Date 2021/10/26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PeopleAreaRequest {


    /**
     * 地区名称
     */
    private String name;

    /**
     * 代表省市区的字段
     * */
    private String field;

    /**
     * 列表查询分页参数.
     */
    private PagingRequest paging;

    /**
     * 列表排序查询参数.
     */
    private SortingRequest sorting;
}
