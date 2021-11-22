/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund;

import com.zqykj.app.service.annotation.*;
import com.zqykj.common.vo.Direction;
import com.zqykj.common.vo.PageRequest;
import com.zqykj.common.vo.SortRequest;
import com.zqykj.util.BigDecimalUtil;
import lombok.*;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <h1> 交易统计分析结果查询实体 </h1>
 */
@Setter
@Getter
@NoArgsConstructor
// 对于es 来说聚合需要带出字段名称,需要再类上加上 @Local 或者 @Opposite 或者 @Agg 注解指定聚合名称, @Key(name="hits")是固定的
// 其他数据源正常 eg. mysql 可以直接带出field (field 的名称  就等于 我们定义的聚合名称)
// eg. customerName 定义了  @Local(name = "customer_name", showField = true)
@Agg(name = "local_hits")
@Key
@JsonIgnoreProperties(ignoreUnknown = true)
public class TradeStatisticalAnalysisResult extends FundPartAnalysisResult {

    // 开户名称
    @Agg(name = "customer_name", showField = true)
    @Key(name = "customer_name")
    private String customerName;

    // 开户证件号码
    @Agg(name = "customer_identity_card", showField = true)
    @Key(name = "customer_identity_card")
    private String customerIdentityCard;

    // 开户银行
    @Agg(name = "bank", showField = true)
    @Key(name = "bank")
    private String bank;

    // 账号
    @Agg(name = "query_account", showField = true)
    @Key(name = "query_account")
    private String queryAccount;

    // 交易卡号
    @Agg(name = "query_card", showField = true)
    @Key(name = "query_card")
    private String tradeCard;

    @SuppressWarnings("all")
    public static List<TradeStatisticalAnalysisResult> tradeStatisticalAnalysisResultSortAndPage(List<TradeStatisticalAnalysisResult> results,
                                                                                                 PageRequest pageRequest, SortRequest sortRequest) {

        String property = sortRequest.getProperty();
        Direction order = sortRequest.getOrder();
        Integer from = pageRequest.getPage();
        Integer size = pageRequest.getPageSize();
        List<TradeStatisticalAnalysisResult> sortedList;
        // TODO 暂时只对数值字段进行排序
        if ("tradeTotalTimes".equals(property)) {
            Comparator<Integer> comparator = Comparator.reverseOrder();
            if (order.isAscending()) {
                comparator = Comparator.naturalOrder();
            }
            sortedList = results.stream().sorted(Comparator.comparing(TradeStatisticalAnalysisResult::getTradeTotalTimes, comparator))
                    .collect(Collectors.toList());
        } else if ("tradeTotalAmount".equals(property)) {
            Comparator<BigDecimal> comparator = Comparator.reverseOrder();
            if (order.isAscending()) {
                comparator = Comparator.naturalOrder();
            }
            sortedList = results.stream().sorted(Comparator.comparing(TradeStatisticalAnalysisResult::getTradeTotalAmount, comparator))
                    .collect(Collectors.toList());
        } else if ("creditsTimes".equals(property)) {
            Comparator<Integer> comparator = Comparator.reverseOrder();
            if (order.isAscending()) {
                comparator = Comparator.naturalOrder();
            }
            sortedList = results.stream().sorted(Comparator.comparing(TradeStatisticalAnalysisResult::getCreditsTimes, comparator))
                    .collect(Collectors.toList());
        } else if ("creditsAmount".equals(property)) {
            Comparator<BigDecimal> comparator = Comparator.reverseOrder();
            if (order.isAscending()) {
                comparator = Comparator.naturalOrder();
            }
            sortedList = results.stream().sorted(Comparator.comparing(TradeStatisticalAnalysisResult::getCreditsAmount, comparator))
                    .collect(Collectors.toList());
        } else if ("payOutTimes".equals(property)) {
            Comparator<Integer> comparator = Comparator.reverseOrder();
            if (order.isAscending()) {
                comparator = Comparator.naturalOrder();
            }
            sortedList = results.stream().sorted(Comparator.comparing(TradeStatisticalAnalysisResult::getPayOutTimes, comparator))
                    .collect(Collectors.toList());
        } else if ("payOutAmount".equals(property)) {
            Comparator<BigDecimal> comparator = Comparator.reverseOrder();
            if (order.isAscending()) {
                comparator = Comparator.naturalOrder();
            }
            sortedList = results.stream().sorted(Comparator.comparing(TradeStatisticalAnalysisResult::getPayOutAmount, comparator))
                    .collect(Collectors.toList());
        } else if ("tradeNet".equals(property)) {
            Comparator<BigDecimal> comparator = Comparator.reverseOrder();
            if (order.isAscending()) {
                comparator = Comparator.naturalOrder();
            }
            sortedList = results.stream().sorted(Comparator.comparing(TradeStatisticalAnalysisResult::getTradeNet, comparator))
                    .collect(Collectors.toList());
        } else if ("earliestTradingTime".equals(property)) {
            sortedList = results.stream().sorted(((o1, o2) -> {
                LocalDateTime parseO1 = LocalDateTime.parse(o1.getEarliestTradingTime(), format);
                LocalDateTime parseO2 = LocalDateTime.parse(o2.getEarliestTradingTime(), format);
                if (order.isAscending()) {
                    return parseO1.compareTo(parseO2);
                } else {
                    return parseO2.compareTo(parseO1);
                }
            })).collect(Collectors.toList());
        } else if ("latestTradingTime".equals(property)) {
            sortedList = results.stream().sorted(((o1, o2) -> {
                LocalDateTime parseO1 = LocalDateTime.parse(o1.getLatestTradingTime(), format);
                LocalDateTime parseO2 = LocalDateTime.parse(o2.getLatestTradingTime(), format);
                if (order.isAscending()) {
                    return parseO1.compareTo(parseO2);
                } else {
                    return parseO2.compareTo(parseO1);
                }
            })).collect(Collectors.toList());
        } else {
            // TODO 其余按照自然排序处理
            Comparator<String> comparator = Comparator.reverseOrder();
            sortedList = results.stream().sorted().collect(Collectors.toList());
        }
        // 分页
        return sortedList.stream().skip(from).limit(size).collect(Collectors.toList());
    }

    @SuppressWarnings("all")
    public static TradeStatisticalAnalysisResult mergeTradeStatisticalAnalysisBankFlow(List<TradeStatisticalAnalysisResult> bankFlowList) {

        TradeStatisticalAnalysisResult bankFlow = new TradeStatisticalAnalysisResult();
        // 交易总次数
        int tradeTotalTimes = 0;
        // 交易总金额
        BigDecimal tradeTotalAmount = new BigDecimal("0.00");
        // 入账次数
        int creditsTimes = 0;
        // 入账金额
        BigDecimal creditsAmount = new BigDecimal("0.00");
        // 出账次数
        int payOutTimes = 0;
        // 出账金额
        BigDecimal payOutAmount = new BigDecimal("0.00");
        // 交易净和
        BigDecimal tradeNet = new BigDecimal("0.00");
        String minDate = "0";
        String maxDate = "0";
        for (TradeStatisticalAnalysisResult analysisBankFlow : bankFlowList) {
            tradeTotalTimes += analysisBankFlow.getTradeTotalTimes();
            tradeTotalAmount = BigDecimalUtil.add(tradeTotalAmount, analysisBankFlow.getTradeTotalAmount());
            creditsTimes += analysisBankFlow.getCreditsTimes();
            creditsAmount = BigDecimalUtil.add(creditsAmount, analysisBankFlow.getCreditsAmount());
            payOutTimes += analysisBankFlow.getPayOutTimes();
            payOutAmount = BigDecimalUtil.add(payOutAmount, analysisBankFlow.getPayOutAmount());
            tradeNet = BigDecimalUtil.add(tradeNet, analysisBankFlow.getTradeNet());

            LocalDateTime curMinDate = LocalDateTime.parse(analysisBankFlow.getEarliestTradingTime(), format);
            LocalDateTime curMaxDate = LocalDateTime.parse(analysisBankFlow.getLatestTradingTime(), format);

            if (minDate.equals("0") && maxDate.equals("0")) {

                minDate = analysisBankFlow.getEarliestTradingTime();
                maxDate = analysisBankFlow.getEarliestTradingTime();
            } else {
                LocalDateTime beforeMinDate = LocalDateTime.parse(minDate, format);
                LocalDateTime beforeMaxDate = LocalDateTime.parse(maxDate, format);

                if (curMinDate.isBefore(beforeMinDate)) {

                    minDate = analysisBankFlow.getEarliestTradingTime();
                }
                if (curMaxDate.isAfter(beforeMaxDate)) {
                    maxDate = analysisBankFlow.getLatestTradingTime();
                }
            }
        }
        TradeStatisticalAnalysisResult analysisBankFlow = bankFlowList.get(0);
        // 开户名称
        bankFlow.setCustomerName(analysisBankFlow.getCustomerName());
        // 开户证件号码
        bankFlow.setCustomerIdentityCard(analysisBankFlow.getCustomerIdentityCard());
        // 开户银行
        bankFlow.setBank(analysisBankFlow.getBank());
        // 账号
        bankFlow.setQueryAccount(analysisBankFlow.getQueryAccount());
        // 交易卡号
        bankFlow.setTradeCard(analysisBankFlow.getTradeCard());
        bankFlow.setTradeTotalTimes(tradeTotalTimes);
        bankFlow.setTradeTotalAmount(tradeTotalAmount);
        bankFlow.setCreditsTimes(creditsTimes);
        bankFlow.setCreditsAmount(creditsAmount);
        bankFlow.setPayOutTimes(payOutTimes);
        bankFlow.setPayOutAmount(payOutAmount);
        bankFlow.setTradeNet(tradeNet);

        return bankFlow;
    }

    public static void calculationDate(TradeStatisticalAnalysisResult bankFlow, long minDate, long maxDate) {

        Instant minInstant = Instant.ofEpochMilli(minDate);
        String minTime = LocalDateTime.ofInstant(minInstant, ZoneOffset.ofHours(0)).format(format);
        Instant maxInstant = Instant.ofEpochMilli(maxDate);
        String maxTime = LocalDateTime.ofInstant(maxInstant, ZoneOffset.ofHours(0)).format(format);
        bankFlow.setEarliestTradingTime(minTime);
        bankFlow.setLatestTradingTime(maxTime);
    }

    public static void amountReservedTwo(TradeStatisticalAnalysisResult bankFlow) {
        bankFlow.setTradeTotalAmount(BigDecimalUtil.value(bankFlow.getTradeTotalAmount()));
        bankFlow.setCreditsAmount(BigDecimalUtil.value(bankFlow.getCreditsAmount()));
        bankFlow.setPayOutAmount(BigDecimalUtil.value(bankFlow.getPayOutAmount()));
        bankFlow.setTradeNet(BigDecimalUtil.value(bankFlow.getTradeNet()));
    }
}
