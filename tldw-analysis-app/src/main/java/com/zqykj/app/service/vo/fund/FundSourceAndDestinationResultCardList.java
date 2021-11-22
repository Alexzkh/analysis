package com.zqykj.app.service.vo.fund;


import com.zqykj.app.service.annotation.Agg;
import com.zqykj.app.service.annotation.Key;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.math.BigDecimal;


/**
 * <h1>资金来源去向列表返回实体 </h1>
 **/
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Agg(name = "local_hits")
@Key
@JsonIgnoreProperties(ignoreUnknown = true)
public class FundSourceAndDestinationResultCardList {

    // 开户名称
    @Agg(name = "customer_name", showField = true)
    @Key(name = "customer_name")
    private String customerName;

    // 开户证件号码
    @Agg(name = "customer_identity_card", showField = true)
    @Key(name = "customer_identity_card")
    private String customerIdentityCard;

    // 开户银行
    @Agg(name = "bank", showField = true)
    @Key(name = "bank")
    private String bank;

    // 账号
    @Agg(name = "query_account", showField = true)
    @Key(name = "query_account")
    private String queryAccount;

    // 交易卡号
    @Agg(name = "query_card", showField = true)
    @Key(name = "query_card")
    private String tradeCard;
    // 交易总金额
    @Agg(name = "local_trade_amount")
    @Key(name = "valueAsString")
    private BigDecimal tradeTotalAmount;

    // 入账次数
    @Agg(name = "local_credits_times")
    @Key(name = "docCount")
    private int creditsTimes;

    // 入账金额
    @Agg(name = "local_credits_amount")
    @Key(name = "valueAsString")
    private BigDecimal creditsAmount;

    // 出账次数
    @Agg(name = "local_out_times")
    @Key(name = "docCount")
    private int payOutTimes;

    // 出账金额
    @Agg(name = "local_out_amount")
    @Key(name = "valueAsString")
    private BigDecimal payOutAmount;

    // 交易净和
    @Agg(name = "local_trade_net")
    @Key(name = "valueAsString")
    private BigDecimal tradeNet;

    // 最早交易时间
    @Agg(name = "local_min_date")
    @Key(name = "valueAsString")
    private String earliestTradingTime;

    // 最晚交易时间
    @Agg(name = "local_max_date")
    @Key(name = "valueAsString")
    private String latestTradingTime;

    public enum EntityMapping {
        customerName, customerIdentityCard, bank,tradeTotalTimes, tradeTotalAmount,
        creditsTimes, creditsAmount, payOutTimes, payOutAmount, tradeNet, earliestTradingTime, latestTradingTime,
        local_source, opposite_source, total
    }
}
