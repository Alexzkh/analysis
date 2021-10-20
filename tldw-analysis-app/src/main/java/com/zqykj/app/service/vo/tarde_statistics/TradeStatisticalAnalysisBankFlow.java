/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.tarde_statistics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * <h1> 交易统计分析银行流水返回实体 </h1>
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TradeStatisticalAnalysisBankFlow {

    // 开户名称
    private String customerName;

    // 开户证件号码
    private String customerIdentityCard;

    // 开户银行
    private String bank;

    // 账号
    private String queryAccount;

    // 交易卡号
    private String queryCard;

    // 交易总次数
    private int tradeTotalTimes;

    // 交易总金额
    private BigDecimal tradeTotalAmount;

    // 入账次数
    private int creditsTimes;

    // 入账金额
    private BigDecimal creditsAmount;

    // 出账次数
    private int payOutTimes;

    // 出账金额
    private BigDecimal payOutAmount;

    // 交易净和
    private BigDecimal tradeNet;

    // 最早交易时间
    private String earliestTradingTime;

    // 最晚交易时间
    private String latestTradingTime;
}
