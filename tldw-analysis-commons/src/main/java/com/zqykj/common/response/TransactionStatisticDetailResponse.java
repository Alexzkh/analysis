package com.zqykj.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @Description: 交易统计列表数据详情返回体
 * @Author zhangkehou
 * @Date 2021/10/13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionStatisticDetailResponse {

    private Integer total;
}
