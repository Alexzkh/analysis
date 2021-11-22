package com.zqykj.common.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @Description: 资金来源去向折线图返回结果
 * @Author zhangkehou
 * @Date 2021/11/18
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FundsSourceAndDestinationLineChart {

    /**
     * 交易时间
     */
    private String time;

    /**
     * 交易金额
     */
    private BigDecimal transactionMoney;
}
