package com.zqykj.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 资产趋势返回响应体.
 * @Author zhangkehou
 * @Date 2021/10/19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssetTrendsResponse {

    /**
     * 交易流水总表id;
     */
    private Long id;

    /**
     * 案件编号.
     */
    private String caseId;

    /**
     * 按照年、季度、月份统计的日期.
     */
    private String date;

    /**
     * 交易净额.
     **/
    private double transactionNet;

    /**
     * 收入总额.
     */
    private double totolIncome;

    /**
     * 支出总额.
     */
    private double totalExpenditure;

    /**
     * 交易金额.
     */
    private double totalTransactionMoney;
}
