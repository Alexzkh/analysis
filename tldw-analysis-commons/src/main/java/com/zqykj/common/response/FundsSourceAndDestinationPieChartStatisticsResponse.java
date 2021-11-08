package com.zqykj.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @Description: 资金来源去向饼图统计结果返回
 * @Author zhangkehou
 * @Date 2021/11/6
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FundsSourceAndDestinationPieChartStatisticsResponse {


    /**
     * 交易账户总个数
     */
    private Integer transcationAccountTotalNumber;

    /**
     * 来源账户个数
     */
    private Integer sourceAccountNumber;

    /**
     * 来源账户百分比
     */
    private String sourcePercentage;

    /**
     * 去向账户个数
     */
    private Integer destinationAccountNumber;

    /**
     * 来源账户百分比
     */
    private String destinationPercentage;

    /**
     * 交易账户总金额
     */
    private BigDecimal transcationAccountTotalMoney;

    /**
     * 来源账户金额
     */
    private BigDecimal sourceAccountMoney;

    /**
     * 来源账户百分比
     */
    private String sourceMoneyPercentage;

    /**
     * 去向账户金额
     */
    private BigDecimal destinationAccountMoney;

    /**
     * 来源账户金额百分比
     */
    private String destinationMoneyPercentage;

}
