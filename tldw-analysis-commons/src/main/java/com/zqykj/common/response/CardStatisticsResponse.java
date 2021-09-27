package com.zqykj.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Description: 卡的相关统计结果
 * @Author zhangkehou
 * @Date 2021/9/22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardStatisticsResponse {

    public CardStatisticsResponse(AggregationResult aggregationResult) throws ParseException {
        this.earliestTradingTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(aggregationResult.getEarliestTradingTime());
        this.queryCard = aggregationResult.getCard();
        this.entriesAmount =new BigDecimal(aggregationResult.getEntriesAmount()).setScale(2, RoundingMode.HALF_UP);
        this.entriesNums = aggregationResult.getEntriesNums();
        this.LatestTradingTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(aggregationResult.getLatestTradingTime());
        this.outGoingAmount = new BigDecimal(aggregationResult.getOutGoingAmount()).setScale(2, RoundingMode.HALF_UP);
        this.outGoingNums = aggregationResult.getOutGoingNums();
        this.transactionTotalNums = aggregationResult.getTransactionTotalNums();
        this.transactionNetAmount = new BigDecimal(aggregationResult.getTransactionNetAmount()).setScale(2, RoundingMode.HALF_UP);
        this.transactionTotalAmount = new BigDecimal(aggregationResult.getTransactionTotalAmount()).setScale(2, RoundingMode.HALF_UP);
        //
    }

    /**
     * 调单卡号（查询卡号）
     */
    private String queryCard;

    /**
     * 开户银行
     */
    private String bank;

    /**
     * 账户开户名称
     */
    private String customerName;

    /**
     * 开户人证件号码
     */
    private String customerIdentityId;

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
    private BigDecimal entriesAmount;

    /**
     * 出账笔数
     */
    private Long outGoingNums;

    /**
     * 出账金额
     */
    private BigDecimal outGoingAmount;

    /**
     * 交易净额
     */
    private BigDecimal transactionNetAmount;

    /**
     * 交易总金额
     */
    private BigDecimal transactionTotalAmount;

    /**
     * 最早交易时间
     */
    private Date earliestTradingTime;

    /**
     * 最晚交易时间
     */
    private Date LatestTradingTime;
}
