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
public class FundSourceAndDestinationNetLineChart {

    // 出账金额
    @Opposite(name = "transaction_money_sum", sortName = "transaction_money_sum")
    @Key(name = "valueAsString")
    private BigDecimal payOutAmount;

    // 交易时间
    @Local(name = "date_histogram_trading_time")
    @Opposite(name = "date_histogram_trading_time")
    @Key(name = "keyAsString")
    @DateString
    private String tradingTime;


    public enum EntityMapping {
        payOutAmount,tradingTime
    }




}
