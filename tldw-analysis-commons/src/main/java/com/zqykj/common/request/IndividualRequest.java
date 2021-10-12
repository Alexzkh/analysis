package com.zqykj.common.request;

import lombok.*;

/**
 * @Description: 调单个体查询请求体
 * @Author zhangkehou
 * @Date 2021/9/23
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndividualRequest {

    /**
     * 模糊查询关键字
     **/
    private String keyword;

    /**
     * 排序参数
     **/
    private SortingRequest sortingRequest;

    /**
     * 分页参数
     **/
    private PagingRequest pagingRequest;

    /**
     * 聚合查询条件中的字段值
     **/
    private String field;

    /**
     * 构建聚合查询条件的值
     **/
    private String value;

    /**
     * 案件id
     **/
    private String caseId;
}
