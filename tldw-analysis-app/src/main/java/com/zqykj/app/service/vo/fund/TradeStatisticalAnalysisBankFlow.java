/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund;

import com.zqykj.common.vo.Direction;
import com.zqykj.common.vo.SortRequest;
import com.zqykj.util.BigDecimalUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <h1> 交易统计分析银行流水返回实体 </h1>
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TradeStatisticalAnalysisBankFlow {

    // 开户名称
    @Local(name = "customer_name")
    @Opposite(name = "transaction_opposite_name")
    @Key(name = "hits")
    @Hits
    private String customerName;

    // 开户证件号码
    @Local(name = "customer_identity_card")
    @Opposite(name = "transaction_opposite_certificate_number")
    @Hits
    private String customerIdentityCard;

    // 开户银行
    @Local(name = "bank")
    @Opposite(name = "transaction_opposite_account_open_bank")
    @Hits
    private String bank;

    // 账号
    @Local(name = "query_account")
    @Opposite(name = "transaction_opposite_account")
    @Hits
    private String queryAccount;

    // 交易卡号
    @Local(name = "query_card")
    @Opposite(name = "transaction_opposite_card")
    @Hits
    private String tradeCard;

    // 交易总次数
    @Local(name = "local_trade_total", type = "local_trade_total")
    @Opposite(name = "opposite_trade_total", type = "opposite_trade_total")
    @Key(name = "value")
    private int tradeTotalTimes = 0;

    // 交易总金额
    @Local(name = "local_trade_amount", sortName = "local_trade_amount")
    @Opposite(name = "opposite_trade_amount", sortName = "opposite_trade_amount")
    @Key(name = "valueAsString")
    private BigDecimal tradeTotalAmount;

    // 入账次数
    @Local(name = "local_credits_times", sortName = "local_credits_times._count")
    @Opposite(name = "opposite_credits_times", sortName = "opposite_credits_times._count")
    @Key(name = "docCount")
    private int creditsTimes = 0;

    // 入账金额
    @Local(name = "local_credits_amount", sortName = "local_credits_times>local_credits_amount")
    @Opposite(name = "opposite_credits_amount", sortName = "opposite_credits_times>opposite_credits_amount")
    @Key(name = "valueAsString")
    private BigDecimal creditsAmount;

    // 出账次数
    @Local(name = "local_out_times", sortName = "local_out_times._count")
    @Opposite(name = "opposite_out_times", sortName = "opposite_out_times._count")
    @Key(name = "docCount")
    private int payOutTimes = 0;

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
    @Key(name = "value")
    @DateString
    private String earliestTradingTime;

    // 最晚交易时间
    @Local(name = "local_max_date", type = "date", sortName = "local_max_date")
    @Opposite(name = "opposite_max_date", type = "date", sortName = "opposite_max_date")
    @Key(name = "value")
    @DateString
    private String latestTradingTime;

    public enum EntityMapping {
        customerName, customerIdentityCard, bank, queryAccount, tradeCard, tradeTotalTimes, tradeTotalAmount,
        creditsTimes, creditsAmount, payOutTimes, payOutAmount, tradeNet, earliestTradingTime, latestTradingTime,
        local_source, opposite_source, total
    }

    public static final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static List<TradeStatisticalAnalysisBankFlow> sortingAndPageOnMemory(List<TradeStatisticalAnalysisBankFlow> list, SortRequest sortRequest,
                                                                                com.zqykj.common.vo.PageRequest pageRequest) {

        List<TradeStatisticalAnalysisBankFlow> finalResult = new ArrayList<>();
        // 排序的方向
        // 先排序
        if (null == sortRequest) {

            // 默认按照交易统计金额排序
            finalResult = list.stream().sorted(Comparator.comparing(TradeStatisticalAnalysisBankFlow::getTradeTotalAmount, Comparator.reverseOrder())).collect(Collectors.toList());
        } else {

            String propertyName = sortRequest.getProperty();
            Optional<Field> optionalField = Arrays.stream(TradeStatisticalAnalysisBankFlow.class.getDeclaredFields()).filter(field -> field.getName().equals(propertyName)).findFirst();
            if (optionalField.isPresent()) {
                // 允许访问此字段
                Field field = optionalField.get();
                ReflectionUtils.makeAccessible(field);
                if (String.class.isAssignableFrom(field.getType())) {
                    Comparator<String> stringComparator;
                    if (Direction.DESC == sortRequest.getOrder()) {
                        stringComparator = Comparator.reverseOrder();
                    } else {
                        stringComparator = Comparator.naturalOrder();
                    }
                    if (null != field.getAnnotation(DateString.class)) {

                        finalResult = list.stream()
                                .sorted(Comparator.comparing(key -> Objects.requireNonNull(ReflectionUtils.getField(field, key)).toString(), stringComparator))
                                .collect(Collectors.toList());
                    } else {
                        finalResult = list.stream()
                                .sorted(Comparator.comparing(key -> BigDecimalUtil.longValue(Objects.requireNonNull(ReflectionUtils.getField(field, key)).toString()), Comparator.reverseOrder()))
                                .collect(Collectors.toList());
                    }
                } else if (Integer.TYPE.isAssignableFrom(field.getType())) {
                    Comparator<Integer> intComparator;
                    if (Direction.DESC == sortRequest.getOrder()) {
                        intComparator = Comparator.reverseOrder();
                    } else {
                        intComparator = Comparator.naturalOrder();
                    }
                    finalResult = list.stream()
                            .sorted(Comparator.comparing(key -> (int) Objects.requireNonNull(ReflectionUtils.getField(field, key)), intComparator))
                            .collect(Collectors.toList());
                } else if (BigDecimal.class.isAssignableFrom(field.getType())) {

                    Comparator<BigDecimal> bigDecimalComparator;
                    if (Direction.DESC == sortRequest.getOrder()) {
                        bigDecimalComparator = Comparator.reverseOrder();
                    } else {
                        bigDecimalComparator = Comparator.naturalOrder();
                    }
                    finalResult = list.stream()
                            .sorted(Comparator.comparing(key -> (BigDecimal) Objects.requireNonNull(ReflectionUtils.getField(field, key)), bigDecimalComparator))
                            .collect(Collectors.toList());
                }
            } else {
                // 默认按照交易统计金额排序
                finalResult = list.stream().sorted(Comparator.comparing(TradeStatisticalAnalysisBankFlow::getTradeTotalAmount, Comparator.reverseOrder())).collect(Collectors.toList());
            }
        }
        // 分页
        if (null == pageRequest) {

            // 默认每页25条
            return finalResult.stream().limit(25).collect(Collectors.toList());
        } else {
            int page = com.zqykj.common.vo.PageRequest.getOffset(pageRequest.getPage(), pageRequest.getPageSize());
            return finalResult.stream().skip(page).limit(pageRequest.getPageSize()).collect(Collectors.toList());
        }
    }

    public static TradeStatisticalAnalysisBankFlow mergeTradeStatisticalAnalysisBankFlow(List<TradeStatisticalAnalysisBankFlow> bankFlowList) {

        TradeStatisticalAnalysisBankFlow bankFlow = new TradeStatisticalAnalysisBankFlow();
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
        // 最早交易时间
        long minDate = 0L;
        long maxDate = 0L;
        // 最晚交易时间

        for (TradeStatisticalAnalysisBankFlow analysisBankFlow : bankFlowList) {
            tradeTotalTimes += analysisBankFlow.getTradeTotalTimes();
            tradeTotalAmount = BigDecimalUtil.add(tradeTotalAmount, analysisBankFlow.getTradeTotalAmount());
            creditsTimes += analysisBankFlow.getCreditsTimes();
            creditsAmount = BigDecimalUtil.add(creditsAmount, analysisBankFlow.getCreditsAmount());
            payOutTimes += analysisBankFlow.getPayOutTimes();
            payOutAmount = BigDecimalUtil.add(payOutAmount, analysisBankFlow.getPayOutAmount());
            tradeNet = BigDecimalUtil.add(tradeNet, analysisBankFlow.getTradeNet());
            long nextDate = BigDecimalUtil.longValue(analysisBankFlow.getEarliestTradingTime());
            if (minDate == 0L) {
                minDate = nextDate;
            } else {
                if (minDate > BigDecimalUtil.longValue(analysisBankFlow.getEarliestTradingTime())) {
                    minDate = nextDate;
                }
            }
            if (nextDate > maxDate) {
                maxDate = nextDate;
            }
        }
        TradeStatisticalAnalysisBankFlow analysisBankFlow = bankFlowList.get(0);
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
        calculationDate(bankFlow, minDate, maxDate);
        return bankFlow;
    }

    public static void calculationDate(TradeStatisticalAnalysisBankFlow bankFlow, long minDate, long maxDate) {
        // 由于es 的时区问题(es 是0时区设置的),需要对最早和最晚日期分别加上 +8小时
        Instant minInstant = Instant.ofEpochMilli(minDate);
        String minTime = LocalDateTime.ofInstant(minInstant, ZoneOffset.ofHours(8)).format(format);
        Instant maxInstant = Instant.ofEpochMilli(maxDate);
        String maxTime = LocalDateTime.ofInstant(maxInstant, ZoneOffset.ofHours(8)).format(format);
        bankFlow.setEarliestTradingTime(minTime);
        bankFlow.setLatestTradingTime(maxTime);
    }
}
