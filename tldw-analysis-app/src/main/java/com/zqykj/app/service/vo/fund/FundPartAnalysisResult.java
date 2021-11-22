/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund;

import com.zqykj.app.service.annotation.Agg;
import com.zqykj.app.service.annotation.Key;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

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
    @Agg(name = "local_trade_total", sortName = "local_trade_total")
    @Key(name = "value")
    private int tradeTotalTimes;

    // 交易总金额
    @Agg(name = "local_trade_amount", sortName = "local_trade_amount")
    @Key(name = "valueAsString")
    private BigDecimal tradeTotalAmount;

    // 入账次数
    @Agg(name = "local_credits_times", sortName = "local_credits_times._count")
    @Key(name = "docCount")
    private int creditsTimes;

    // 入账金额
    @Agg(name = "local_credits_amount", sortName = "local_credits_times>local_credits_amount")
    @Key(name = "valueAsString")
    private BigDecimal creditsAmount;

    // 出账次数
    @Agg(name = "local_out_times", sortName = "local_out_times._count")
    @Key(name = "docCount")
    private int payOutTimes;

    // 出账金额
    @Agg(name = "local_out_amount", sortName = "local_out_times>local_out_amount")
    @Key(name = "valueAsString")
    private BigDecimal payOutAmount;

    // 交易净和
    @Agg(name = "local_trade_net", sortName = "local_trade_net")
    @Key(name = "valueAsString")
    private BigDecimal tradeNet;

    // 最早交易时间
    @Agg(name = "local_min_date", sortName = "local_min_date")
    @Key(name = "valueAsString")
    private String earliestTradingTime;

    // 最晚交易时间
    @Agg(name = "local_max_date", sortName = "local_max_date")
    @Key(name = "valueAsString")
    private String latestTradingTime;

    public static final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
}
