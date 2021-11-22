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
@Agg(name = "local_hits")
@Key
@JsonIgnoreProperties(ignoreUnknown = true)
public class FundSourceAndDestinationBankRecord {

    // 开户名称
    @Agg(name = "customer_name")
    @Key(name = "customer_name")
    private String customerName;

    // 开户证件号码
    @Agg(name = "customer_identity_card")
    @Key(name = "customer_identity_card")
    private String customerIdentityCard;


    // 交易总金额
    @Agg(name = "transaction_money_sum")
    @Key(name = "valueAsString")
    private BigDecimal tradeTotalAmount;

    // 最早交易时间
    @Agg(name = "local_min_date")
    @Key(name = "value")
    private String earliestTradingTime;

    // 最晚交易时间
    @Agg(name = "local_max_date")
    @Key(name = "value")
    private String latestTradingTime;

    public enum EntityMapping {
        customerName, customerIdentityCard, tradeTotalAmount, earliestTradingTime, latestTradingTime,
    }


}
