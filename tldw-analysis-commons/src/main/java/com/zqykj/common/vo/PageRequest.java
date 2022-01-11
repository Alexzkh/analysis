/**
 * @作者 Mcj
 */
package com.zqykj.common.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * <h1> 分页请求 </h1>
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PageRequest {

    private Integer page = 1;
    private Integer pageSize = 25;
    private Integer start = 0;

    private SortRequest sortRequest;

    // 计算总页数
    public static int getTotalPages(int total, int pageSize) {
        return total == 0 ? 0 : (int) Math.ceil((double) total / (double) pageSize);
    }

    public static int getPage(int page) {

        return page <= 0 ? 0 : page - 1;
    }

    public static int getOffset(int page, int pageSize) {
        return page * pageSize;
    }

    public static PageRequest of(int page, int pageSize, SortRequest sortRequest) {
        PageRequest pageRequest = new PageRequest();
        pageRequest.setPage(page);
        pageRequest.setPageSize(pageSize);
        pageRequest.setSortRequest(sortRequest);
        return pageRequest;
    }

    public static PageRequest of(int page, int pageSize) {
        PageRequest pageRequest = new PageRequest();
        pageRequest.setPage(page);
        pageRequest.setPageSize(pageSize);
        return pageRequest;
    }
}
