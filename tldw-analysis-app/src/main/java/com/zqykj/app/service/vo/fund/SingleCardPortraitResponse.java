package com.zqykj.app.service.vo.fund;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 单卡画像返回体
 *
 * @author: SunChenYu
 * @date: 2021年11月15日 11:55:50
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SingleCardPortraitResponse implements Serializable {
    /**
     * 调单账号（查询账号）
     */
    private String queryAccount;

    /**
     * 调单卡号（查询卡号）
     */
    private String queryCard;

    /**
     * 客户名称
     */
    private String customerName;

    /**
     * 客户身份证号码
     */
    private String customerIdentityCard;

    /**
     * 开户银行
     */
    private String bank;

    /**
     * 开户日期
     */
    private String accountOpeningDate;

    /**
     * 入账金额
     */
    private BigDecimal entriesAmount;

    /**
     * 出账金额
     */
    private BigDecimal outGoingAmount;

    /**
     * 交易总金额
     */
    private BigDecimal transactionTotalAmount;

    /**
     * 最早交易时间
     */
    private String earliestTradingTime;

    /**
     * 最晚交易时间
     */
    private String latestTradingTime;

    /**
     * 账户余额
     */
    private BigDecimal transactionBalance;

}
