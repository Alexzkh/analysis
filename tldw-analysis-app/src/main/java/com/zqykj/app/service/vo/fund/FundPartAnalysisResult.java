/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund;

import com.alibaba.excel.annotation.ExcelProperty;
import com.zqykj.app.service.annotation.Agg;
import com.zqykj.app.service.annotation.Key;
import com.zqykj.app.service.annotation.Sort;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * <h1> 提取公共的一些统计分析值 </h1>
 * <p>
 * eg. 交易总次数、交易总金额、入账次数、入账金额、出账次数、出账金额、交易净和、最早交易时间、最晚交易时间
 */
@Setter
@Getter
@NoArgsConstructor
public class FundPartAnalysisResult {

    // 交易总次数
    @Agg(name = "local_trade_total")
    @Key(name = "value")
    @Sort(name = "local_trade_total")
    @ExcelProperty(value = "交易总次数", order = 20)
    private int tradeTotalTimes;

    // 交易总金额
    @Agg(name = "local_trade_amount")
    @Key(name = "valueAsString")
    @Sort(name = "local_trade_amount")
    @ExcelProperty(value = "交易总金额", order = 21)
    private BigDecimal tradeTotalAmount;

    // 入账次数
    @Agg(name = "local_credits_times")
    @Key(name = "docCount")
    @Sort(name = "local_credits_times._count")
    @ExcelProperty(value = "入账次数", order = 22)
    private int creditsTimes;

    // 入账金额
    @Agg(name = "local_credits_amount")
    @Key(name = "valueAsString")
    @Sort(name = "local_credits_times>local_credits_amount")
    @ExcelProperty(value = "入账金额", order = 23)
    private BigDecimal creditsAmount;

    // 出账次数
    @Agg(name = "local_out_times")
    @Key(name = "docCount")
    @Sort(name = "local_out_times._count")
    @ExcelProperty(value = "出账次数", order = 24)
    private int payOutTimes;

    // 出账金额
    @Agg(name = "local_out_amount")
    @Key(name = "valueAsString")
    @Sort(name = "local_out_times>local_out_amount")
    @ExcelProperty(value = "出账金额", order = 25)
    private BigDecimal payOutAmount;

    // 交易净和
    @Agg(name = "local_trade_net")
    @Key(name = "valueAsString")
    @Sort(name = "local_trade_net")
    @ExcelProperty(value = "交易净和", order = 26)
    private BigDecimal tradeNet;

    // 最早交易时间
    @Agg(name = "local_min_date")
    @Key(name = "valueAsString")
    @Sort(name = "local_min_date")
    @ExcelProperty(value = "最早交易时间", order = 27)
    private String earliestTradingTime;

    // 最晚交易时间
    @Agg(name = "local_max_date")
    @Key(name = "valueAsString")
    @Sort(name = "local_max_date")
    @ExcelProperty(value = "最晚交易时间", order = 28)
    private String latestTradingTime;
}
