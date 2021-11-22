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
public class FundSourceAndDestinationResultCardList {

    // 开户名称
    @Local(name = "customer_name")
    @Opposite(name = "customer_name")
    @Key(name = "hits")
    @Hits
    private String customerName;

    // 开户证件号码
    @Local(name = "customer_identity_card")
    @Opposite(name = "customer_identity_card")
    @Hits
    private String customerIdentityCard;

    // 开户银行
    @Local(name = "bank")
    @Opposite(name = "bank")
    @Hits
    private String bank;
    // 交易卡号
    @Local(name = "query_card")
    @Opposite(name = "query_card")
    @Hits
    private String tradeCard;

    // 交易总次数
    @Local(name = "local_trade_total", type = "local_trade_total")
    @Opposite(name = "opposite_trade_total", type = "opposite_trade_total")
    @Key(name = "value")
    private int tradeTotalTimes = 0;

    // 交易总金额
    @Local(name = "local_trade_amount", sortName = "local_trade_amount")
    @Opposite(name = "opposite_trade_amount", sortName = "opposite_trade_amount")
    @Key(name = "valueAsString")
    private BigDecimal tradeTotalAmount;

    // 入账次数
    @Local(name = "local_credits_times", sortName = "local_credits_times._count")
    @Opposite(name = "opposite_credits_times", sortName = "opposite_credits_times._count")
    @Key(name = "docCount")
    private int creditsTimes = 0;

    // 入账金额
    @Local(name = "local_credits_amount", sortName = "local_credits_times>local_credits_amount")
    @Opposite(name = "opposite_credits_amount", sortName = "opposite_credits_times>opposite_credits_amount")
    @Key(name = "valueAsString")
    private BigDecimal creditsAmount;

    // 出账次数
    @Local(name = "local_out_times", sortName = "local_out_times._count")
    @Opposite(name = "opposite_out_times", sortName = "opposite_out_times._count")
    @Key(name = "docCount")
    private int payOutTimes = 0;

    // 出账金额
    @Local(name = "local_out_amount", sortName = "local_out_times>local_out_amount")
    @Opposite(name = "opposite_out_amount", sortName = "opposite_out_times>opposite_out_amount")
    @Key(name = "valueAsString")
    private BigDecimal payOutAmount;

    // 交易净和
    @Local(name = "local_trade_net", sortName = "local_trade_net")
    @Opposite(name = "opposite_trade_net", sortName = "opposite_trade_net")
    @Key(name = "valueAsString")
    private BigDecimal tradeNet;

    // 最早交易时间
    @Local(name = "local_min_date", type = "date", sortName = "local_min_date")
    @Opposite(name = "opposite_min_date", type = "date", sortName = "opposite_min_date")
    @Key(name = "value")
    @DateString
    private String earliestTradingTime;

    // 最晚交易时间
    @Local(name = "local_max_date", type = "date", sortName = "local_max_date")
    @Opposite(name = "opposite_max_date", type = "date", sortName = "opposite_max_date")
    @Key(name = "value")
    @DateString
    private String latestTradingTime;

    public enum EntityMapping {
        customerName, customerIdentityCard, bank,tradeTotalTimes, tradeTotalAmount,
        creditsTimes, creditsAmount, payOutTimes, payOutAmount, tradeNet, earliestTradingTime, latestTradingTime,
        local_source, opposite_source, total
    }
}
