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
@Agg(name = "local_hits")
@Key
@JsonIgnoreProperties(ignoreUnknown = true)
public class FundSourceAndDestinationNetLineChart {

    // 出账金额
    @Agg(name = "local_out_amount")
    @Key(name = "valueAsString")
    private BigDecimal payOutAmount;

    // 交易时间
    @Agg(name = "local_max_date")
    @Key(name = "valueAsString")
    private String tradingTime;


    public enum EntityMapping {
        payOutAmount,tradingTime
    }




}
