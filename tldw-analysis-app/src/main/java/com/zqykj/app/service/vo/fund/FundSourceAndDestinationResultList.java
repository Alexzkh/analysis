package com.zqykj.app.service.vo.fund;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;


/**
 * <h1>资金来源去向列表返回实体 </h1>
 **/
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FundSourceAndDestinationResultList {

    // 开户名称
    @Opposite(name = "transaction_opposite_name")
    @Key(name = "hits")
    @Hits
    private String customerName;

    // 开户证件号码
    @Opposite(name = "transaction_opposite_certificate_number")
    @Hits
    private String customerIdentityCard;


    // 交易总次数
    @Opposite(name = "opposite_trade_total", type = "opposite_trade_total")
    @Key(name = "value")
    private int tradeTotalTimes = 0;

    // 交易总金额
    @Opposite(name = "opposite_trade_amount", sortName = "opposite_trade_amount")
    @Key(name = "valueAsString")
    private BigDecimal tradeTotalAmount;

    // 入账次数
    @Opposite(name = "opposite_credits_times", sortName = "opposite_credits_times._count")
    @Key(name = "docCount")
    private int creditsTimes = 0;

    // 入账金额
    @Opposite(name = "opposite_credits_amount", sortName = "opposite_credits_times>opposite_credits_amount")
    @Key(name = "valueAsString")
    private BigDecimal creditsAmount;

    // 出账次数
    @Opposite(name = "opposite_out_times", sortName = "opposite_out_times._count")
    @Key(name = "docCount")
    private int payOutTimes = 0;

    // 出账金额
    @Opposite(name = "opposite_out_amount", sortName = "opposite_out_times>opposite_out_amount")
    @Key(name = "valueAsString")
    private BigDecimal payOutAmount;

    // 交易净和
    @Opposite(name = "opposite_trade_net", sortName = "opposite_trade_net")
    @Key(name = "valueAsString")
    private BigDecimal tradeNet;

    // 最早交易时间
    @Opposite(name = "opposite_min_date", type = "date", sortName = "opposite_min_date")
    @Key(name = "value")
    @DateString
    private String earliestTradingTime;

    // 最晚交易时间
    @Opposite(name = "opposite_max_date", type = "date", sortName = "opposite_max_date")
    @Key(name = "value")
    @DateString
    private String latestTradingTime;

    public enum EntityMapping {
        customerName, customerIdentityCard, tradeTotalTimes, tradeTotalAmount,
        creditsTimes, creditsAmount, payOutTimes, payOutAmount, tradeNet, earliestTradingTime, latestTradingTime,
        local_source, opposite_source, total
    }
}
