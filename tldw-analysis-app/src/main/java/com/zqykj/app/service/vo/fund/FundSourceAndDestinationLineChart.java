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
 * <h1> 来源去向折线图返回实体 </h1>
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FundSourceAndDestinationLineChart {

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

    // 交易时间
    @Agg(name = "date_histogram_trading_time")
    @Key(name = "keyAsString")
    private String tradingTime;


    public enum EntityMapping {
        creditsTimes, creditsAmount, payOutTimes, payOutAmount,tradingTime
    }




}
