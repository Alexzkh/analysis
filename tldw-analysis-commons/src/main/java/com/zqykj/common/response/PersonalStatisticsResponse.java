package com.zqykj.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Description: 调单个体统计响应实体
 * @Author zhangkehou
 * @Date 2021/9/22
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PersonalStatisticsResponse implements Serializable {


    public PersonalStatisticsResponse(AggregationResult aggregationResult) throws ParseException {
        this.accountCardNums = aggregationResult.getAccountCardNums();
        this.earliestTradingTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(aggregationResult.getEarliestTradingTime());
        this.customerIdentityId = aggregationResult.getCard();
        this.entriesAmount =new BigDecimal(aggregationResult.getEntriesAmount()).setScale(2, RoundingMode.HALF_UP);
        this.entriesNums = aggregationResult.getEntriesNums();
        this.LatestTradingTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(aggregationResult.getLatestTradingTime());
        this.outGoingAmount = new BigDecimal(aggregationResult.getOutGoingAmount()).setScale(2, RoundingMode.HALF_UP);
        this.outGoingNums = aggregationResult.getOutGoingNums();
        this.transactionTotalNums = aggregationResult.getTransactionTotalNums();
        this.transactionNetAmount = new BigDecimal(aggregationResult.getTransactionNetAmount()).setScale(2, RoundingMode.HALF_UP);
        this.transactionTotalAmount = new BigDecimal(aggregationResult.getTransactionTotalAmount()).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 账户开户名称
     */
    private String customerName;

    /**
     * 开户人证件号码
     */
    private String customerIdentityId;

    /**
     * 手机号码
     */
    private String phoneNumber;

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
