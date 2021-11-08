package com.zqykj.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * @Description: 资金来源去向趋势统一返回体
 * @Author zhangkehou
 * @Date 2021/11/6
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FundsSourceAndDestinationTrendResponse {


    /**
     * 交易时间
     */
    private String time;

    /**
     * 交易金额
     */
    private BigDecimal transactionMoney;


}
