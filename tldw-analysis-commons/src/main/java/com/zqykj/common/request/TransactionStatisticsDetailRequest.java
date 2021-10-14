package com.zqykj.common.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 交易统计-列表详情
 * @Author zhangkehou
 * @Date 2021/10/13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionStatisticsDetailRequest {

    /**
     * 查询卡号.
     */
    private String cardNumber;

    /**
     * 查询参数(模糊查询、分页以及排序参数).
     */
    private QueryRequest queryRequest;
}
