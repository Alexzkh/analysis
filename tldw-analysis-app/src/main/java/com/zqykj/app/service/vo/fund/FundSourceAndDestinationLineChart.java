package com.zqykj.app.service.vo.fund;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

/**
 * <h1> 来源去向折线图返回实体 </h1>
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FundSourceAndDestinationLineChart {

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

    // 交易时间
    @Local(name = "date_histogram_trading_time")
    @Opposite(name = "date_histogram_trading_time")
    @Key(name = "keyAsString")
    @DateString
    private String tradingTime;


    public enum EntityMapping {
        creditsTimes, creditsAmount, payOutTimes, payOutAmount,tradingTime
    }




}
