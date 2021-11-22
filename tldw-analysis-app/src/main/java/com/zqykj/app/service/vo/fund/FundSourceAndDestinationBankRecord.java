package com.zqykj.app.service.vo.fund;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * <h1> 资金来源去向返回实体 </h1>
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FundSourceAndDestinationBankRecord {

    // 开户名称
    @Local(name = "customer_name")
    @Opposite(name = "transaction_opposite_name")
    @Key(name = "hits")
    @Hits
    private String customerName;

    // 开户证件号码
    @Local(name = "customer_identity_card")
    @Opposite(name = "transaction_opposite_certificate_number")
    @Hits
    private String customerIdentityCard;



    // 交易总金额
    @Local(name = "transaction_money_sum", sortName = "transaction_money_sum")
    @Opposite(name = "transaction_money_sum", sortName = "transaction_money_sum")
    @Key(name = "valueAsString")
    private BigDecimal tradeTotalAmount;

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
        customerName, customerIdentityCard, tradeTotalAmount, earliestTradingTime, latestTradingTime,
    }


}
