/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.NumberUtil;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.zqykj.app.service.annotation.Agg;
import com.zqykj.app.service.annotation.Key;
import com.zqykj.app.service.annotation.Sort;
import com.zqykj.util.BigDecimalUtil;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * <h1> 交易字段类型统计 </h1>
 */
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
//@JsonIgnoreProperties(value = {"sumTradeAmount", "sumTradeTotalTimes", "sumCreditsAmount", "sumCreditsTimes", "sumPayoutTimes", "sumPayoutAmount"})
@ExcelIgnoreUnannotated
public class TransactionFieldTypeStatisticsResult {

    /**
     * 字段分组后的结果
     */
    @Agg(name = "field_group")
    @Key(name = "keyAsString")
    @ExcelProperty(value = "统计字段内容")
    private String fieldTypeGroupContent;

    /**
     * 交易总金额
     */
    @Agg(name = "trade_amount")
    @Key(name = "value")
    @Sort(name = "trade_amount")
    @ExcelProperty(value = "交易总金额")
    private BigDecimal tradeTotalAmount;

    /**
     * 汇总交易金额
     */
    @Agg(name = "sum_trade_amount")
    @Key(name = "value")
    private BigDecimal sumTradeAmount;

    /**
     * 总金额占比
     */
    private BigDecimal totalAmountProportion;

    @ExcelProperty(value = "总金额占比")
    private String totalAmountProportionStr;

    /**
     * 交易次数
     */
    @Agg(name = "trade_times")
    @Key(name = "value")
    @Sort(name = "trade_times")
    @ExcelProperty(value = "交易次数")
    private int tradeTotalTimes;

    /**
     * 汇总交易总次数
     */
    @Agg(name = "sum_trade_times")
    @Key(name = "value")
    private int sumTradeTotalTimes;

    /**
     * 交易次数占比
     */
    private BigDecimal tradeTimesProportion;

    @ExcelProperty(value = "交易次数占比")
    private String tradeTimesProportionStr;

    /**
     * 入账金额
     */
    @Agg(name = "credits_amount")
    @Key(name = "value")
    @Sort(name = "filter_credit>credits_amount")
    @ExcelProperty(value = "入账金额")
    private BigDecimal creditsAmount;

    /**
     * 汇总入账金额
     */
    @Agg(name = "sum_credits_amount")
    @Key(name = "value")
    private BigDecimal sumCreditsAmount;

    /**
     * 入账金额占比
     */
    private BigDecimal creditsAmountProportion;

    @ExcelProperty(value = "入账金额占比")
    private String creditsAmountProportionStr;

    /**
     * 入账次数
     */
    @Agg(name = "credits_times")
    @Key(name = "value")
    @Sort(name = "credits_times._count")
    @ExcelProperty(value = "入账次数")
    private int creditsTimes;

    /**
     * 汇总入账次数
     */
    @Agg(name = "sum_credits_times")
    @Key(name = "value")
    private int sumCreditsTimes;

    /**
     * 入账次数占比
     */
    private BigDecimal creditTimesProportion;

    @ExcelProperty(value = "入账次数占比")
    private String creditTimesProportionStr;

    /**
     * 出账金额
     */
    @Agg(name = "payout_amount")
    @Key(name = "value")
    @Sort(name = "filter_payout>payout_amount")
    @ExcelProperty(value = "出账金额")
    private BigDecimal payoutAmount;

    /**
     * 汇总出账金额
     */
    @Agg(name = "sum_payout_amount")
    @Key(name = "value")
    private BigDecimal sumPayoutAmount;

    /**
     * 出账金额占比
     */
    private BigDecimal payoutAmountProportion;

    @ExcelProperty(value = "出账金额占比")
    private String payoutAmountProportionStr;

    /**
     * 出账次数
     */
    @Agg(name = "payout_times")
    @Key(name = "value")
    @Sort(name = "payout_times._count")
    @ExcelProperty(value = "出账次数")
    private int payoutTimes;

    @Agg(name = "sum_payout_times")
    @Key(name = "value")
    private int sumPayoutTimes;

    /**
     * 出账次数占比
     */
    private BigDecimal payoutTimesProportion;

    @ExcelProperty(value = "出账次数占比")
    private String payoutTimesProportionStr;

    /**
     * <h2> 计算占比数据(总金额占比、交易次数占比、入账金额占比、入账次数占比、出账金额占比、出账次数占比)  </h2>
     */
    public static void calculateProportionData(List<TransactionFieldTypeStatisticsResult> statisticsResults,
                                               List<TransactionFieldTypeCustomResults> customResults, boolean isExport) {

        BigDecimal sumTradeAmount = new BigDecimal(0);
        int sumTradeTimes = 0;
        BigDecimal sumCreditsAmount = new BigDecimal(0);
        int sumCreditsTimes = 0;
        BigDecimal sumPayoutAmount = new BigDecimal(0);
        int sumPayoutTimes = 0;
        // 自定义数据要先汇总
        for (TransactionFieldTypeCustomResults customResult : customResults) {
            sumTradeAmount = BigDecimalUtil.add(sumTradeAmount, customResult.getTradeTotalAmount());
            sumTradeTimes += customResult.getTradeTotalTimes();
            sumCreditsAmount = BigDecimalUtil.add(sumCreditsAmount, customResult.getCreditsAmount());
            sumCreditsTimes += customResult.getCreditsTimes();
            sumPayoutAmount = BigDecimalUtil.add(sumPayoutAmount, customResult.getPayoutAmount());
            sumPayoutTimes += customResult.getPayoutTimes();
        }
        // 将自定义的汇总金额 与 正常查询的汇总金额合并(才能计算下面的占比数据)
        TransactionFieldTypeStatisticsResult sumDataPart = statisticsResults.get(0);
        sumDataPart.setSumTradeAmount(BigDecimalUtil.add(sumTradeAmount, sumDataPart.getSumTradeAmount()));
        sumDataPart.setSumTradeTotalTimes(sumTradeTimes + sumDataPart.getSumTradeTotalTimes());
        sumDataPart.setSumCreditsAmount(BigDecimalUtil.add(sumCreditsAmount, sumDataPart.getSumCreditsAmount()));
        sumDataPart.setSumCreditsTimes(sumCreditsTimes + sumDataPart.getSumCreditsTimes());
        sumDataPart.setSumPayoutAmount(BigDecimalUtil.add(sumPayoutAmount, sumDataPart.getSumPayoutAmount()));
        sumDataPart.setSumPayoutTimes(sumPayoutTimes + sumDataPart.getSumPayoutTimes());
        statisticsResults.forEach(e -> {
            // 金额保留2位
            amountReservedTwo(e);
            // 总金额占比
            BigDecimal sumTradeAmountProportion = BigDecimalUtil.mul(BigDecimalUtil.div(e.getTradeTotalAmount(), sumDataPart.getSumTradeAmount(), 4), 100);
            e.setTotalAmountProportion(sumTradeAmountProportion);
            // 交易次数占比
            BigDecimal tradeTimesProportion = BigDecimalUtil.mul(BigDecimalUtil.div(e.getTradeTotalTimes(), sumDataPart.getSumTradeTotalTimes(), 4), 100);
            e.setTradeTimesProportion(tradeTimesProportion);
            // 入账金额占比
            BigDecimal creditsAmountProportion = BigDecimalUtil.mul(BigDecimalUtil.div(e.getCreditsAmount(), sumDataPart.getSumCreditsAmount(), 4), 100);
            e.setCreditsAmountProportion(creditsAmountProportion);
            // 入账次数占比
            BigDecimal creditsTimesProportion = BigDecimalUtil.mul(BigDecimalUtil.div(e.getCreditsTimes(), sumDataPart.getSumCreditsTimes(), 4), 100);
            e.setCreditTimesProportion(creditsTimesProportion);
            // 出账金额占比
            BigDecimal payoutAmountProportion = BigDecimalUtil.mul(BigDecimalUtil.div(e.getPayoutAmount(), sumDataPart.getSumPayoutAmount(), 4), 100);
            e.setPayoutAmountProportion(payoutAmountProportion);
            // 出账次数占比
            BigDecimal payoutTimesProportion = BigDecimalUtil.mul(BigDecimalUtil.div(e.getPayoutTimes(), sumDataPart.getSumPayoutTimes(), 4), 100);
            e.setPayoutTimesProportion(payoutTimesProportion);
            if (isExport) {
                // 用于导出,带%
                e.setTotalAmountProportionStr(sumTradeAmountProportion + "%");
                e.setTradeTimesProportionStr(tradeTimesProportion + "%");
                e.setCreditsAmountProportionStr(creditsAmountProportion + "%");
                e.setCreditTimesProportionStr(creditsTimesProportion + "%");
                e.setPayoutAmountProportionStr(payoutAmountProportion + "%");
                e.setPayoutTimesProportionStr(payoutTimesProportion + "%");
            }
        });
    }

    /**
     * <h2> 将自定义归类查询数据转化成交易字段类型统计数据 </h2>
     */
    public static List<TransactionFieldTypeStatisticsResult> convertStatisticsResults(List<TransactionFieldTypeCustomResults> customResults) {

        List<TransactionFieldTypeStatisticsResult> statisticsResults = new ArrayList<>();
        CopyOptions copyOptions = new CopyOptions();
        copyOptions.setIgnoreNullValue(true);
        copyOptions.setIgnoreError(true);
        customResults.forEach(e -> {
            TransactionFieldTypeStatisticsResult results = new TransactionFieldTypeStatisticsResult();
            BeanUtil.copyProperties(e, results, copyOptions);
            statisticsResults.add(results);
        });
        return statisticsResults;
    }

    public static void amountReservedTwo(TransactionFieldTypeStatisticsResult result) {
        result.setTradeTotalAmount(BigDecimalUtil.value(result.getTradeTotalAmount()));
        result.setCreditsAmount(BigDecimalUtil.value(result.getCreditsAmount()));
        result.setPayoutAmount(BigDecimalUtil.value(result.getPayoutAmount()));
    }

    public static void main(String[] args) {
        double div = NumberUtil.div(20, 268598519.1867);
        double mul = NumberUtil.mul(div, 100);
    }
}
