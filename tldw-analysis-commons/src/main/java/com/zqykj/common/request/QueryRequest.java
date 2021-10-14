package com.zqykj.common.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: TODO
 * @Author zhangkehou
 * @Date 2021/10/13
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QueryRequest {

    /**
     * 模糊查询关键字.
     * */
    private String keyword;

    /**
     * 列表查询分页参数.
     * */
    private PagingRequest paging;

    /**
     * 列表排序查询参数.
     * */
    private SortingRequest sorting;
}
