package com.zqykj.common.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @Description: TODO
 * @Author zhangkehou
 * @Date 2021/9/23
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PagingRequest {

    private int page;

    private int pageSize;

}
