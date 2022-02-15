/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund;

import com.zqykj.app.service.annotation.Agg;
import com.zqykj.app.service.annotation.Key;
import com.zqykj.app.service.annotation.Sort;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * <h1> 交易字段类型统计 </h1>
 */
@Setter
@Getter
public class TransactionFieldTypeStatisticsResult {

    /** 字段分组后的结果 */
    @Agg(name = "field_group_content")
    @Key(name = "keyAsString")
    private String fieldGroupContent;

    /** 交易总金额 */
    @Agg(name = "trade_total_amount")
    @Key(name = "value")
    @Sort(name = "trade_total_amount")
    private BigDecimal tradeTotalAmount;

    /** 总金额占比 */
    @Agg(name = "total_amount_proportion")
    @Key(name = "value")
    @Sort(name = "total_amount_proportion")
    private BigDecimal totalAmountProportion;

    /** 交易次数 */
    @Agg(name = "trade_times")
    @Key(name = "value")
    @Sort(name = "trade_times")
    private int tradeTotalTimes;

    /** 交易次数占比 */
    @Agg(name = "trade_times_proportion")
    @Key(name = "value")
    @Sort(name = "trade_times_proportion")
    private int tradeTimesProportion;

    /** 入账金额 */
    @Agg(name = "credits_amount")
    @Key(name = "value")
    @Sort(name = "filter_credit>credits_amount")
    private BigDecimal creditsAmount;

    /** 入账金额占比 */
    @Agg(name = "credits_amount_proportion")
    @Key(name = "value")
    private BigDecimal creditsAmountProportion;

    /** 入账次数 */
    @Agg(name = "credits_times")
    @Key(name = "value")
    private int creditsTimes;

    /** 入账次数占比 */
    @Agg(name = "credits_times_proportion")
    @Key(name = "value")
    private BigDecimal creditTimesProportion;

    /** 出账金额 */
    @Agg(name = "payout_amount")
    @Key(name = "value")
    @Sort(name = "filter_payout>payout_amount")
    private BigDecimal payoutAmount;

    /** 出账金额占比 */
    @Agg(name = "payout_amount_proportion")
    @Key(name = "value")
    private BigDecimal payoutAmountProportion;

    /** 出账次数 */
    @Agg(name = "payout_times")
    @Key(name = "value")
    private int payoutTimes;

    /** 出账次数占比 */
    @Agg(name = "payout_times_proportion")
    @Key(name = "value")
    private BigDecimal payoutTimesProportion;
}
