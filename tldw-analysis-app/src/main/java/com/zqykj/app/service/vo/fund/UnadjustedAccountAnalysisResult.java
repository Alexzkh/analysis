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

    // 账户名称
    @Agg(name = "customer_name", showField = true)
    @Key(name = "customer_name")
    private String customerName;

    // 对方开户行
    @Agg(name = "bank", showField = true)
    @Key(name = "bank")
    private String bank;

    // 关联账户数
    @Agg(name = "linkedAccountTimes")
    @Key(name = "value")
    @Sort(name = "linkedAccountTimes")
    private int numberOfLinkedAccounts;

    // 交易总次数
    @Agg(name = "tradeTotalTimes")
    @Key(name = "value")
    @Sort(name = "tradeTotalTimes")
    private int tradeTotalTimes;

    // 交易总金额
    @Agg(name = "tradeTotalAmount")
    @Key(name = "value")
    @Sort(name = "tradeTotalAmount")
    private BigDecimal tradeTotalAmount;

    // 入账金额
    @Agg(name = "creditSum")
    @Key(name = "value")
    @Sort(name = "filterCredit>creditSum")
    private BigDecimal creditsAmount;

    // 出账金额
    @Agg(name = "payoutSum")
    @Key(name = "value")
    @Sort(name = "filterPayout>payoutSum")
    private BigDecimal payOutAmount;

    // 交易净和
    @Agg(name = "tradeNetSum")
    @Key(name = "value")
    @Sort(name = "tradeNetSum")
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
        result.setCreditsAmount(BigDecimalUtil.value(result.getCreditsAmount()));
        result.setPayOutAmount(BigDecimalUtil.value(result.getPayOutAmount()));
        result.setTradeNet(BigDecimalUtil.value(result.getTradeNet()));
    }
}
