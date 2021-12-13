/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund.middle;

import com.zqykj.app.service.annotation.Agg;
import com.zqykj.app.service.annotation.Key;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * <h1> 调单卡号入账的(给定一组卡号筛选出) </h1>
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreditAdjustCards {

    /**
     * 调单卡号
     */
    @Agg(name = "query_card_terms")
    @Key(name = "keyAsString")
    private String adjustCard;

    /**
     * 入账次数
     */
    @Agg(name = "credits_times")
    @Key(name = "value")
    private int creditsTimes;

    /**
     * 入账金额
     */
    @Agg(name = "credit_amount")
    @Key(name = "valueAsString")
    private BigDecimal creditsAmount;
}
