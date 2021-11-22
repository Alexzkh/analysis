/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund;

import com.zqykj.app.service.annotation.Agg;
import com.zqykj.app.service.annotation.Key;
import com.zqykj.app.service.annotation.Local;
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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <h1> 交易汇聚结果查询实体 </h1>
 */
@Setter
@Getter
@NoArgsConstructor
// 对于es 来说聚合需要带出字段名称,需要再类上加上 @Agg 注解指定聚合名称, @Key(name="hits")是固定的
// 其他数据源正常 eg. mysql 可以直接带出field (field 的名称  就等于 我们定义的聚合名称)
// eg. customerName 定义了  @Agg(name = "customer_name", showField = true)
@Agg(name = "local_hits")
@Key
@JsonIgnoreProperties(ignoreUnknown = true)
public class TradeConvergenceAnalysisResult extends FundPartAnalysisResult {

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

    // 对方开户名称
    @Agg(name = "transaction_opposite_name", showField = true)
    @Key(name = "transaction_opposite_name")
    private String oppositeCustomerName;

    // 对方开户证件号码
    @Agg(name = "transaction_opposite_certificate_number", showField = true)
    @Key(name = "transaction_opposite_certificate_number")
    private String oppositeIdentityCard;

    // 对方开户银行
    @Agg(name = "transaction_opposite_account_open_bank", showField = true)
    @Key(name = "transaction_opposite_account_open_bank")
    private String oppositeBank;

    // 对方卡号
    @Agg(name = "transaction_opposite_card", showField = true)
    @Key(name = "transaction_opposite_card")
    private String oppositeTradeCard;


    public static void calculationDate(TradeConvergenceAnalysisResult bankFlow, long minDate, long maxDate) {

        Instant minInstant = Instant.ofEpochMilli(minDate);
        String minTime = LocalDateTime.ofInstant(minInstant, ZoneOffset.ofHours(0)).format(format);
        Instant maxInstant = Instant.ofEpochMilli(maxDate);
        String maxTime = LocalDateTime.ofInstant(maxInstant, ZoneOffset.ofHours(0)).format(format);
        bankFlow.setEarliestTradingTime(minTime);
        bankFlow.setLatestTradingTime(maxTime);
    }

    public static void amountReservedTwo(TradeConvergenceAnalysisResult bankFlow) {
        bankFlow.setTradeTotalAmount(BigDecimalUtil.value(bankFlow.getTradeTotalAmount()));
        bankFlow.setCreditsAmount(BigDecimalUtil.value(bankFlow.getCreditsAmount()));
        bankFlow.setPayOutAmount(BigDecimalUtil.value(bankFlow.getPayOutAmount()));
        bankFlow.setTradeNet(BigDecimalUtil.value(bankFlow.getTradeNet()));
    }


    public static String fetchGroupKey(TradeConvergenceAnalysisResult convergenceAnalysisResult) {
        return convergenceAnalysisResult.getTradeCard() + "#" + convergenceAnalysisResult.getOppositeTradeCard();

    }

    @SuppressWarnings("all")
    public static TradeConvergenceAnalysisResult mergeTradeConvergenceAnalysisResult(List<TradeConvergenceAnalysisResult> convergenceResults) {

        TradeConvergenceAnalysisResult convergenceResult = new TradeConvergenceAnalysisResult();
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
        for (TradeConvergenceAnalysisResult convergenceAnalysisResult : convergenceResults) {
            tradeTotalTimes += convergenceAnalysisResult.getTradeTotalTimes();
            tradeTotalAmount = BigDecimalUtil.add(tradeTotalAmount, convergenceAnalysisResult.getTradeTotalAmount());
            creditsTimes += convergenceAnalysisResult.getCreditsTimes();
            creditsAmount = BigDecimalUtil.add(creditsAmount, convergenceAnalysisResult.getCreditsAmount());
            payOutTimes += convergenceAnalysisResult.getPayOutTimes();
            payOutAmount = BigDecimalUtil.add(payOutAmount, convergenceAnalysisResult.getPayOutAmount());
            tradeNet = BigDecimalUtil.add(tradeNet, convergenceAnalysisResult.getTradeNet());

            LocalDateTime curMinDate = LocalDateTime.parse(convergenceAnalysisResult.getEarliestTradingTime(), format);
            LocalDateTime curMaxDate = LocalDateTime.parse(convergenceAnalysisResult.getLatestTradingTime(), format);

            if (minDate.equals("0") && maxDate.equals("0")) {

                minDate = convergenceAnalysisResult.getEarliestTradingTime();
                maxDate = convergenceAnalysisResult.getEarliestTradingTime();
            } else {
                LocalDateTime beforeMinDate = LocalDateTime.parse(minDate, format);
                LocalDateTime beforeMaxDate = LocalDateTime.parse(maxDate, format);

                if (curMinDate.isBefore(beforeMinDate)) {

                    minDate = convergenceAnalysisResult.getEarliestTradingTime();
                }
                if (curMaxDate.isAfter(beforeMaxDate)) {
                    maxDate = convergenceAnalysisResult.getLatestTradingTime();
                }
            }
        }
        TradeConvergenceAnalysisResult convergenceAnalysisResult = convergenceResults.get(0);
        // 开户名称
        convergenceResult.setCustomerName(convergenceAnalysisResult.getCustomerName());
        // 开户证件号码
        convergenceResult.setCustomerIdentityCard(convergenceAnalysisResult.getCustomerIdentityCard());
        // 开户银行
        convergenceResult.setBank(convergenceAnalysisResult.getBank());
        // 账号
        convergenceResult.setQueryAccount(convergenceAnalysisResult.getQueryAccount());
        // 交易卡号
        convergenceResult.setTradeCard(convergenceAnalysisResult.getTradeCard());
        // 对方开户名称
        convergenceResult.setOppositeCustomerName(convergenceAnalysisResult.getOppositeCustomerName());
        // 对方证件号码
        convergenceResult.setOppositeIdentityCard(convergenceAnalysisResult.getOppositeIdentityCard());
        // 对方开户银行
        convergenceResult.setOppositeBank(convergenceAnalysisResult.getOppositeBank());
        // 对方交易卡号
        convergenceResult.setOppositeTradeCard(convergenceAnalysisResult.getOppositeTradeCard());
        convergenceResult.setTradeTotalTimes(tradeTotalTimes);
        convergenceResult.setTradeTotalAmount(tradeTotalAmount);
        convergenceResult.setCreditsTimes(creditsTimes);
        convergenceResult.setCreditsAmount(creditsAmount);
        convergenceResult.setPayOutTimes(payOutTimes);
        convergenceResult.setPayOutAmount(payOutAmount);
        convergenceResult.setTradeNet(tradeNet);
        return convergenceResult;
    }

    @SuppressWarnings("all")
    public static List<TradeConvergenceAnalysisResult> tradeConvergenceAnalysisResultSortAndPage(List<TradeConvergenceAnalysisResult> results,
                                                                                                 PageRequest pageRequest, SortRequest sort) {
        String property = sort.getProperty();
        Direction order = sort.getOrder();
        Integer from = pageRequest.getPage();
        Integer size = pageRequest.getPageSize();
        List<TradeConvergenceAnalysisResult> sortedList;
        // TODO 暂时只对数值字段进行排序
        if ("tradeTotalTimes".equals(property)) {
            Comparator<Integer> comparator = Comparator.reverseOrder();
            if (order.isAscending()) {
                comparator = Comparator.naturalOrder();
            }
            sortedList = results.stream().sorted(Comparator.comparing(TradeConvergenceAnalysisResult::getTradeTotalTimes, comparator))
                    .collect(Collectors.toList());
        } else if ("tradeTotalAmount".equals(property)) {
            Comparator<BigDecimal> comparator = Comparator.reverseOrder();
            if (order.isAscending()) {
                comparator = Comparator.naturalOrder();
            }
            sortedList = results.stream().sorted(Comparator.comparing(TradeConvergenceAnalysisResult::getTradeTotalAmount, comparator))
                    .collect(Collectors.toList());
        } else if ("creditsTimes".equals(property)) {
            Comparator<Integer> comparator = Comparator.reverseOrder();
            if (order.isAscending()) {
                comparator = Comparator.naturalOrder();
            }
            sortedList = results.stream().sorted(Comparator.comparing(TradeConvergenceAnalysisResult::getCreditsTimes, comparator))
                    .collect(Collectors.toList());
        } else if ("creditsAmount".equals(property)) {
            Comparator<BigDecimal> comparator = Comparator.reverseOrder();
            if (order.isAscending()) {
                comparator = Comparator.naturalOrder();
            }
            sortedList = results.stream().sorted(Comparator.comparing(TradeConvergenceAnalysisResult::getCreditsAmount, comparator))
                    .collect(Collectors.toList());
        } else if ("payOutTimes".equals(property)) {
            Comparator<Integer> comparator = Comparator.reverseOrder();
            if (order.isAscending()) {
                comparator = Comparator.naturalOrder();
            }
            sortedList = results.stream().sorted(Comparator.comparing(TradeConvergenceAnalysisResult::getPayOutTimes, comparator))
                    .collect(Collectors.toList());
        } else if ("payOutAmount".equals(property)) {
            Comparator<BigDecimal> comparator = Comparator.reverseOrder();
            if (order.isAscending()) {
                comparator = Comparator.naturalOrder();
            }
            sortedList = results.stream().sorted(Comparator.comparing(TradeConvergenceAnalysisResult::getPayOutAmount, comparator))
                    .collect(Collectors.toList());
        } else if ("tradeNet".equals(property)) {
            Comparator<BigDecimal> comparator = Comparator.reverseOrder();
            if (order.isAscending()) {
                comparator = Comparator.naturalOrder();
            }
            sortedList = results.stream().sorted(Comparator.comparing(TradeConvergenceAnalysisResult::getTradeNet, comparator))
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
}
