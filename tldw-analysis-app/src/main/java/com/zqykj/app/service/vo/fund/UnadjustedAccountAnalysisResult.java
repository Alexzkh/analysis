/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund;

import com.zqykj.app.service.annotation.Agg;
import com.zqykj.app.service.annotation.Key;
import com.zqykj.app.service.annotation.Sort;
import com.zqykj.util.BigDecimalUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * <h1> 未调单账户分析结果 </h1>
 */
@Setter
@Getter
@NoArgsConstructor
public class UnadjustedAccountAnalysisResult {

    // 对方卡号(由于查询的表是 BankTransactionRecord,所以查询的是查询卡号,对于BankTransactionFlow 来说是对方卡号)
    @Agg(name = "queryCardGroup")
    @Key(name = "keyAsString")
    private String oppositeCard;

    // 账户开户名称
    @Agg(name = "customer_name", showField = true)
    @Key(name = "customer_name")
    private String accountName;

    // 对方开户行
    @Agg(name = "bank", showField = true)
    @Key(name = "bank")
    private String bank;

    // 关联账户数
    @Agg(name = "linked_account_times")
    @Key(name = "value")
    @Sort(name = "linked_account_times")
    private int numberOfLinkedAccounts;

    // 交易总次数
    @Agg(name = "trade_total_times")
    @Key(name = "value")
    @Sort(name = "trade_total_times")
    private int tradeTotalTimes;

    // 交易总金额
    @Agg(name = "trade_total_amount")
    @Key(name = "value")
    @Sort(name = "trade_total_amount")
    private BigDecimal tradeTotalAmount;

    // 入账总金额
    @Agg(name = "credits_total_amount")
    @Key(name = "value")
    @Sort(name = "filterCredit>credits_total_amount")
    private BigDecimal creditsTotalAmount;

    // 出账总金额
    @Agg(name = "payout_total_amount")
    @Key(name = "value")
    @Sort(name = "filterPayout>payout_total_amount")
    private BigDecimal payoutTotalAmount;

    // 交易总净和
    @Agg(name = "trade_net")
    @Key(name = "value")
    @Sort(name = "trade_net")
    private BigDecimal tradeNet;

    // 计算出的来源特征比
    @Agg(name = "sourceFeatureRatio")
    @Key(name = "value")
    private double sourceRatio;

    // 计算的中转特征比
    @Agg(name = "transitFeatureRatio")
    @Key(name = "value")
    private double transitRatio;

    // 计算的沉淀特征比
    @Agg(name = "depositFeatureRatio")
    @Key(name = "value")
    private double depositRatio;

    // 账户特征(可能有多个) eg. 来源 中转(同时存在)
    private String accountFeature;

    // 数据总量
    private long total;

    public static void amountReservedTwo(UnadjustedAccountAnalysisResult result) {
        result.setTradeTotalAmount(BigDecimalUtil.value(result.getTradeTotalAmount()));
        result.setCreditsTotalAmount(BigDecimalUtil.value(result.getCreditsTotalAmount()));
        result.setPayoutTotalAmount(BigDecimalUtil.value(result.getPayoutTotalAmount()));
        result.setTradeNet(BigDecimalUtil.value(result.getTradeNet()));
    }
}
