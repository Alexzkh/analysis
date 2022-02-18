/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund;

import com.zqykj.app.service.annotation.Agg;
import com.zqykj.app.service.annotation.Key;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * <h1> 交易字段自定义查询结果 </h1>
 */
@Setter
@Getter
public class TransactionFieldTypeCustomResults {

    /**
     * 字段分组后的结果
     */
    private String fieldGroupContent;

    /** 交易总金额汇总 */
    @Agg(name = "sum_trade_amount")
    @Key(name = "value")
    private BigDecimal tradeTotalAmount;

    /** 总金额占比 */
    private BigDecimal totalAmountProportion;

    /** 交易次数汇总 */
    @Agg(name = "sum_trade_times")
    @Key(name = "value")
    private int tradeTotalTimes;

    /** 交易次数占比 */
    private int tradeTimesProportion;

    /** 入账金额汇总 */
    @Agg(name = "sum_credits_amount")
    @Key(name = "value")
    private BigDecimal creditsAmount;

    /** 入账金额占比 */
    private BigDecimal creditsAmountProportion;

    /** 入账次数汇总 */
    @Agg(name = "sum_credits_times")
    @Key(name = "value")
    private int creditsTimes;

    /** 入账次数占比 */
    private BigDecimal creditTimesProportion;

    /** 出账金额汇总 */
    @Agg(name = "sum_payout_amount")
    @Key(name = "value")
    private BigDecimal payoutAmount;

    /** 出账金额占比 */
    private BigDecimal payoutAmountProportion;

    /** 出账次数汇总 */
    @Agg(name = "sum_payout_times")
    @Key(name = "value")
    private int payoutTimes;

    /** 出账次数占比 */
    private BigDecimal payoutTimesProportion;
}
