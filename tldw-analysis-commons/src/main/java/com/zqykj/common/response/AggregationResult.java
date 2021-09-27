package com.zqykj.common.response;

import lombok.*;

import java.io.Serializable;

/**
 * @Description: 聚合结果实体类
 * @Author zhangkehou
 * @Date 2021/9/25
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AggregationResult implements Serializable {


    /**
     * 卡号：证件号码or查询卡号
     */
    private String card;

    /**
     * 调单个体数量
     */
    private Long accountCardNums;

    /**
     * 交易总次数
     */
    private Long transactionTotalNums;

    /**
     * 入账笔数
     */
    private Long entriesNums;

    /**
     * 入账金额
     */
    private Double entriesAmount;

    /**
     * 出账笔数
     */
    private Long outGoingNums;

    /**
     * 出账金额
     */
    private Double outGoingAmount;

    /**
     * 交易净额
     */
    private Double transactionNetAmount;

    /**
     * 交易总金额
     */
    private Double transactionTotalAmount;

    /**
     * 最早交易时间
     */
    private String earliestTradingTime;

    /**
     * 最晚交易时间
     */
    private String LatestTradingTime;
}
