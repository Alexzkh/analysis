package com.zqykj.app.service.vo.fund;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.zqykj.app.service.annotation.Agg;
import com.zqykj.app.service.annotation.Key;
import com.zqykj.app.service.annotation.Sort;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * @Description: 时间规律返回结果
 * @Author zhangkehou
 * @Date 2022/1/5
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TimeRuleAnalysisResult {

    // 交易时间
    @Agg(name = "date_histogram_trading_time")
    @Key(name = "keyAsString")
    private String tradingTime;

    // 交易总次数
    @Agg(name = "local_trade_total")
    @Key(name = "value")
    @Sort(name = "local_trade_total")
    private int tradeTotalTimes;

    // 交易总金额
    @Agg(name = "local_trade_amount")
    @Key(name = "valueAsString")
    @Sort(name = "local_trade_amount")
    private BigDecimal tradeTotalAmount;

    // 总的入账金额
    @Agg(name = "in_total_credits_amount")
    @Key(name = "valueAsString")
    @Sort(name = "in_total_credits_amount")
    private BigDecimal inTotalCreditsAmount;

    // 总的出账金额
    @Agg(name = "out_total_pay_out_amount")
    @Key(name = "valueAsString")
    @Sort(name = "out_total_pay_out_amount")
    private BigDecimal outTotalPayOutAmount;

    // 平均交易金额trade_amount_sum
    @Agg(name = "trade_avg_amount")
    @Key(name = "valueAsString")
    @Sort(name = "trade_avg_amount")
    private BigDecimal tradeAvgAmount;

    // 日期下分桶后的交易金额
    @Agg(name = "date_trade_amount_sum")
    @Key(name = "valueAsString")
    @Sort(name = "date_trade_amount_sum")
    private BigDecimal datetradeAmountSum;

    // 日期下分桶后的交易总次数
    @Agg(name = "date_local_trade_total")
    @Key(name = "value")
    @Sort(name = "date_local_trade_total")
    private int dateTradeTotalTimes;

    // 入账次数
    @Agg(name = "local_credits_times")
    @Key(name = "docCount")
    @Sort(name = "local_credits_times._count")
    private int creditsTimes;

    // 入账金额
    @Agg(name = "local_credits_amount")
    @Key(name = "valueAsString")
    @Sort(name = "local_credits_times>local_credits_amount")
    private BigDecimal creditsAmount;

    // 出账次数
    @Agg(name = "local_out_times")
    @Key(name = "docCount")
    @Sort(name = "local_out_times._count")
    private int payOutTimes;

    // 出账金额
    @Agg(name = "local_out_amount")
    @Key(name = "valueAsString")
    @Sort(name = "local_out_times>local_out_amount")
    private BigDecimal payOutAmount;
}
