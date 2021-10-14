package com.zqykj.infrastructure.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 分页统一返回对象
 * @Author zhangkehou
 * @Date 2021/10/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageServerResponse<T> extends ServerResponse<T> {

    /**
     * 总条数
     */
    private long total;

    /**
     * 页码
     */
    private int page;

    /**
     * 每页条数
     * */
    private int pageSize;
}