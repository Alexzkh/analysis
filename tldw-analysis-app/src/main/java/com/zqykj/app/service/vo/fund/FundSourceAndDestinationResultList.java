package com.zqykj.app.service.vo.fund;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.zqykj.app.service.annotation.Agg;
import com.zqykj.app.service.annotation.Key;
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
@Agg(name = "opposite_hits")
@Key
@JsonIgnoreProperties(ignoreUnknown = true)
public class FundSourceAndDestinationResultList {

    // 开户名称
    @Agg(name = "transaction_opposite_name",showField = true)
    @Key(name = "transaction_opposite_name")
    private String customerName;

    // 开户证件号码
    @Agg(name = "transaction_opposite_certificate_number",showField = true)
    @Key(name = "transaction_opposite_certificate_number")
    private String customerIdentityCard;


    // 交易总次数
    @Agg(name = "opposite_trade_total")
    @Key(name = "value")
    private int tradeTotalTimes = 0;

    // 交易总金额
    @Agg(name = "opposite_trade_amount")
    @Key(name = "valueAsString")
    private BigDecimal tradeTotalAmount;

    // 入账次数
    @Agg(name = "opposite_credits_times")
    @Key(name = "docCount")
    private int creditsTimes = 0;

    // 入账金额
    @Agg(name = "opposite_credits_amount")
    @Key(name = "valueAsString")
    private BigDecimal creditsAmount;

    // 出账次数
    @Agg(name = "opposite_out_times")
    @Key(name = "docCount")
    private int payOutTimes = 0;

    // 出账金额
    @Agg(name = "opposite_out_amount")
    @Key(name = "valueAsString")
    private BigDecimal payOutAmount;

    // 交易净和
    @Agg(name = "opposite_trade_net")
    @Key(name = "valueAsString")
    private BigDecimal tradeNet;

    // 最早交易时间
    @Agg(name = "opposite_min_date")
    @Key(name = "value")
    private String earliestTradingTime;

    // 最晚交易时间
    @Agg(name = "opposite_max_date")
    @Key(name = "value")
    private String latestTradingTime;

    public enum EntityMapping {
        customerName, customerIdentityCard, tradeTotalTimes, tradeTotalAmount,
        creditsTimes, creditsAmount, payOutTimes, payOutAmount, tradeNet, earliestTradingTime, latestTradingTime,
        local_source, opposite_source, total
    }
}
