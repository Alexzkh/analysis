package com.zqykj.common.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

/**
 * @Description: TODO
 * @Author zhangkehou
 * @Date 2021/9/23
 */
@Data
@ToString
@AllArgsConstructor
public class PagingRequest {

    private int page;

    private int pageSize;

}
