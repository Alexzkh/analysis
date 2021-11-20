/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund;

import com.zqykj.app.service.annotation.DateString;
import com.zqykj.app.service.annotation.Key;
import com.zqykj.app.service.annotation.Local;
import com.zqykj.app.service.annotation.Opposite;
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
    @Local(name = "local_trade_total", sortName = "local_trade_total")
    @Opposite(name = "opposite_trade_total", sortName = "opposite_trade_total")
    @Key(name = "value")
    private int tradeTotalTimes;

    // 交易总金额
    @Local(name = "local_trade_amount", sortName = "local_trade_amount")
    @Opposite(name = "opposite_trade_amount", sortName = "opposite_trade_amount")
    @Key(name = "valueAsString")
    private BigDecimal tradeTotalAmount;

    // 入账次数
    @Local(name = "local_credits_times", sortName = "local_credits_times._count")
    @Opposite(name = "opposite_credits_times", sortName = "opposite_credits_times._count")
    @Key(name = "docCount")
    private int creditsTimes;

    // 入账金额
    @Local(name = "local_credits_amount", sortName = "local_credits_times>local_credits_amount")
    @Opposite(name = "opposite_credits_amount", sortName = "opposite_credits_times>opposite_credits_amount")
    @Key(name = "valueAsString")
    private BigDecimal creditsAmount;

    // 出账次数
    @Local(name = "local_out_times", sortName = "local_out_times._count")
    @Opposite(name = "opposite_out_times", sortName = "opposite_out_times._count")
    @Key(name = "docCount")
    private int payOutTimes;

    // 出账金额
    @Local(name = "local_out_amount", sortName = "local_out_times>local_out_amount")
    @Opposite(name = "opposite_out_amount", sortName = "opposite_out_times>opposite_out_amount")
    @Key(name = "valueAsString")
    private BigDecimal payOutAmount;

    // 交易净和
    @Local(name = "local_trade_net", sortName = "local_trade_net")
    @Opposite(name = "opposite_trade_net", sortName = "opposite_trade_net")
    @Key(name = "valueAsString")
    private BigDecimal tradeNet;

    // 最早交易时间
    @Local(name = "local_min_date", type = "date", sortName = "local_min_date")
    @Opposite(name = "opposite_min_date", type = "date", sortName = "opposite_min_date")
    @Key(name = "valueAsString")
    @DateString
    private String earliestTradingTime;

    // 最晚交易时间
    @Local(name = "local_max_date", type = "date", sortName = "local_max_date")
    @Opposite(name = "opposite_max_date", type = "date", sortName = "opposite_max_date")
    @Key(name = "valueAsString")
    @DateString
    private String latestTradingTime;

    public static final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
}
