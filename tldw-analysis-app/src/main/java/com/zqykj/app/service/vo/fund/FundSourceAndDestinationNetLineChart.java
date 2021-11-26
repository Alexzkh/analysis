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
public class FundSourceAndDestinationNetLineChart {

    // 出账金额
    @Agg(name = "transaction_money_sum")
    @Key(name = "valueAsString")
    private BigDecimal payOutAmount;

    // 交易时间
    @Agg(name = "date_histogram_trading_time")
    @Key(name = "keyAsString")
    private String tradingTime;


    public enum EntityMapping {
        payOutAmount,tradingTime
    }




}
