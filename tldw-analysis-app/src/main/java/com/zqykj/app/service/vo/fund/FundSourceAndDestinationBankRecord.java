package com.zqykj.app.service.vo.fund;


import com.zqykj.app.service.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.math.BigDecimal;

/**
 * <h1> 资金来源去向返回实体 </h1>
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Agg(name = "opposite_hits")
@Key
@JsonIgnoreProperties(ignoreUnknown = true)
public class FundSourceAndDestinationBankRecord {

    // 开户名称
    @Agg(name = "transaction_opposite_name",showField = true)
    @Key(name = "transaction_opposite_name")
    private String customerName;

    // 开户证件号码
    @Agg(name = "transaction_opposite_certificate_number",showField = true)
    @Key(name = "transaction_opposite_certificate_number")
    private String customerIdentityCard;


    // 交易总金额
    @Agg(name = "transaction_money_sum")
    @Key(name = "valueAsString")
    private BigDecimal tradeTotalAmount;

    // 最早交易时间
    @Agg(name = "opposite_min_date")
    @Key(name = "valueAsString")
    private String earliestTradingTime;

    // 最晚交易时间
    @Agg(name = "opposite_max_date")
    @Key(name = "valueAsString")
    private String latestTradingTime;

    public enum EntityMapping {
        customerName, customerIdentityCard, tradeTotalAmount, earliestTradingTime, latestTradingTime,
    }


}
