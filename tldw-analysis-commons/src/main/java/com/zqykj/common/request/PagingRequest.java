package com.zqykj.common.request;

import lombok.Data;
import lombok.ToString;

/**
 * @Description: TODO
 * @Author zhangkehou
 * @Date 2021/9/23
 */
@Data
@ToString
public class PagingRequest {

    private int page;

    private int pageSize;

}
