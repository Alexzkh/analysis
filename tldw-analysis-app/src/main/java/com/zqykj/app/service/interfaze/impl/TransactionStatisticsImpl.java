package com.zqykj.app.service.interfaze.impl;

import com.zqykj.app.service.factory.parse.FundTacticsAggResultParseFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.interfaze.ITransactionStatistics;
import com.zqykj.app.service.transform.NumericalConversion;
import com.zqykj.app.service.vo.fund.*;
import com.zqykj.common.constant.Constants;
import com.zqykj.common.core.ServerResponse;
import com.zqykj.common.enums.HistogramStatistic;
import com.zqykj.common.request.TransactionStatisticsDetailRequest;
import com.zqykj.common.response.*;
import com.zqykj.common.request.TradeStatisticalAnalysisPreRequest;
import com.zqykj.common.request.TransactionStatisticsAggs;
import com.zqykj.common.request.TransactionStatisticsRequest;
import com.zqykj.common.vo.Direction;
import com.zqykj.common.vo.SortRequest;
import com.zqykj.domain.*;
import com.zqykj.domain.bank.BankTransactionFlow;
import com.zqykj.factory.AggregationRequestParamFactory;
import com.zqykj.factory.QueryRequestParamFactory;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.repository.EntranceRepository;
import com.zqykj.util.BigDecimalUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.zqykj.common.vo.TimeTypeRequest;
import com.zqykj.core.aggregation.factory.AggregateRequestFactory;
import com.zqykj.parameters.query.QuerySpecialParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description: 交易统计实现类
 * @Author zhangkehou
 * @Date 2021/9/28
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TransactionStatisticsImpl implements ITransactionStatistics {


    private final EntranceRepository entranceRepository;

    private final AggregationRequestParamFactory aggregationRequestParamFactory;

    private final QueryRequestParamFactory queryRequestParamFactory;

    public static final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Value("${buckets.page.init_size}")
    private int initGroupSize;

    @Override
    public TransactionStatisticsResponse calculateStatisticalResults(String caseId, TransactionStatisticsRequest transactionStatisticsRequest) {
        TradeStatisticalAnalysisPreRequest tradeStatisticalAnalysisPreRequest = transactionStatisticsRequest.getTradeStatisticalAnalysisPreRequest();
        TransactionStatisticsAggs transactionStatisticsAggs = transactionStatisticsRequest.getTransactionStatisticsAggs();
        TimeTypeRequest timeTypeRequest = transactionStatisticsAggs.getDateType();

        /**
         * 获取交易金额聚合统计直方图结果.
         * */
        HistogramStatisticResponse histogramStatisticResponse = this.getHistogramStatistics(caseId, tradeStatisticalAnalysisPreRequest, transactionStatisticsAggs);

        /**
         * 获取日期折现图聚合统计结果.
         * */
        TimeGroupTradeAmountSum timeGroupTradeAmountSum = this.getTradeAmountByTime(caseId, tradeStatisticalAnalysisPreRequest, timeTypeRequest);


        // todo 获取卡聚合统计列表

        /**
         * 交易统计返回结果封装.
         * */
        TransactionStatisticsResponse transactionStatisticsResponse = new TransactionStatisticsResponse(histogramStatisticResponse, timeGroupTradeAmountSum, null);


        return transactionStatisticsResponse;
    }


    @Override
    public HistogramStatisticResponse getHistogramStatistics(String caseId, TradeStatisticalAnalysisPreRequest request, TransactionStatisticsAggs transactionStatisticsAggs) {
        List<HistogramStatistic> responseList = new ArrayList<>();
        HistogramStatisticResponse histogramStatisticResponse = new HistogramStatisticResponse();

        // 构建查询参数
        QuerySpecialParams query = this.preQueryTransactionStatisticsAnalysis(caseId, request);
        /**
         * 根据查询条件计算出当前数据中最大值.
         * */
        Map<String, ParsedStats> map = entranceRepository.statsAggs(query, Constants.Individual.FOURTH_AGGREGATE_NAME,
                caseId, BankTransactionFlow.class);
        ParsedStats parsedStats = map.get(Constants.BucketName.STATS);
        Double max = parsedStats.getMax();

        /**
         * 然后根据最大值和传入的区间个数来获取range范围,从而作为直方图聚合参数range的入参.
         * */
        List<Range> ranges = NumericalConversion.intervalConversion(max, transactionStatisticsAggs.getHistorgramNumbers());

        /**
         * 根据range参数和构建好的queryParams做聚合统计算出日志直方图结果.
         * */
        Map queryResultMap = entranceRepository.rangeAggs(query, Constants.Individual.FOURTH_AGGREGATE_NAME
                , caseId,
                ranges, BankTransactionFlow.class);
        /**
         * 转换结果封装业务层数据给前台.
         * */
        queryResultMap.forEach((key, value) -> {
            HistogramStatistic histogramStatistic = HistogramStatistic.builder()
                    .abscissa((String) key)
                    .ordinate((Long) value)
                    .build();
            responseList.add(histogramStatistic);
        });
        histogramStatisticResponse.setHistogramStatisticList(responseList);

        return histogramStatisticResponse;

    }

    @Override
    public QuerySpecialParams preQueryTransactionStatisticsAnalysis(String caseId, TradeStatisticalAnalysisPreRequest request) {
        // 构建查询参数
        return queryRequestParamFactory.createTradeAmountByTimeQuery(request, caseId);
    }

    @Override
    public Page<BankTransactionFlow> accessTransactionStatisticDetail(String caseId, TransactionStatisticsDetailRequest transactionStatisticsDetailRequest) throws Exception {


        PageRequest pageRequest = PageRequest.of(transactionStatisticsDetailRequest.getQueryRequest().getPaging().getPage(),
                transactionStatisticsDetailRequest.getQueryRequest().getPaging().getPageSize(),
                transactionStatisticsDetailRequest.getQueryRequest().getSorting().getOrder().isAscending() ? Sort.Direction.ASC : Sort.Direction.DESC,
                transactionStatisticsDetailRequest.getQueryRequest().getSorting().getProperty());
        Page<BankTransactionFlow> page = entranceRepository.findByCondition(pageRequest, caseId, BankTransactionFlow.class
                , transactionStatisticsDetailRequest.getCardNumber(), caseId, transactionStatisticsDetailRequest.getQueryRequest().getKeyword());
        return page;

    }

    @Override
    public TimeGroupTradeAmountSum getTradeAmountByTime(String caseId, TradeStatisticalAnalysisPreRequest request, TimeTypeRequest timeType) {

        // 构建查询参数
        QuerySpecialParams query = this.preQueryTransactionStatisticsAnalysis(caseId, request);
        // 构建  DateSpecificFormat对象
        Map<String, Object> result = entranceRepository.dateGroupAndSum(query, FundTacticsAnalysisField.TRADING_TIME,
                AggregateRequestFactory.convertFromTimeType(timeType.name()),
                FundTacticsAnalysisField.TRANSACTION_MONEY, BankTransactionFlow.class, caseId);

        TimeGroupTradeAmountSum groupTradeAmountSum = new TimeGroupTradeAmountSum();

        if (!CollectionUtils.isEmpty(result)) {

            Map<String, Object> resultNew = new LinkedHashMap<>();
            if (TimeTypeRequest.h == timeType) {
                // 需要对key 排序
                result.entrySet().stream()
                        .sorted(Comparator.comparing(x -> Integer.parseInt(x.getKey())))
                        .forEachOrdered(x -> resultNew.put(x.getKey(), x.getValue()));
                groupTradeAmountSum.setDates(resultNew.keySet());
                List<String> values = resultNew.values().stream().map(Object::toString).collect(Collectors.toList());
                groupTradeAmountSum.setTradeAmounts(values);
            } else {
                groupTradeAmountSum.setDates(result.keySet());
                List<String> values = result.values().stream().map(Object::toString).collect(Collectors.toList());
                groupTradeAmountSum.setTradeAmounts(values);
            }
        }

        return groupTradeAmountSum;
    }

    public ServerResponse getTransactionStatisticsAnalysisResult(String caseId, TradeStatisticalAnalysisQueryRequest queryRequest) {

        // 计算分组的page 与 pageSize
        // 如果page > initGroupSize
        if (null == queryRequest.getPageRequest()) {

            queryRequest.setPageRequest(new com.zqykj.common.vo.PageRequest());
        }
        int offset = com.zqykj.common.vo.PageRequest.getOffset(queryRequest.getPageRequest().getPage(), queryRequest.getPageRequest().getPageSize());
        if (initGroupSize < offset) {
            queryRequest.setGroupInitPage(offset);
        }
        queryRequest.setGroupInitSize(initGroupSize);
        // 构建交易统计结果查询
        QuerySpecialParams query = queryRequestParamFactory.createTradeStatisticalAnalysisQueryRequest(queryRequest, caseId);

        // 构建交易统计分析聚合查询
        AggregationParams localQuery;
        AggregationParams oppositeQuery;
        AggregationParams filterTotal;

        // 本方
        localQuery = aggregationRequestParamFactory.buildTradeStatisticsAnalysisQueryCardAgg(queryRequest);

        if (!CollectionUtils.isEmpty(queryRequest.getCardNums())) {
            // 对方
            oppositeQuery = aggregationRequestParamFactory.buildTradeStatisticsAnalysisOppositeCardAgg(queryRequest);
            // 设置同级查询(本方 与 对方)
            localQuery.setSiblingAggregation(oppositeQuery);
        } else {

            oppositeQuery = new AggregationParams();
        }
        // 分别设置mapping
        Map<String, String> localMapping = new LinkedHashMap<>();
        Map<String, String> localEntityAggColMapping = new LinkedHashMap<>();
        Map<String, String> oppositeEntityAggColMapping = new LinkedHashMap<>();
        Map<String, String> oppositeMapping = new LinkedHashMap<>();
        ReflectionUtils.doWithFields(TradeStatisticalAnalysisBankFlow.class, field -> {

            Local local = field.getAnnotation(Local.class);
            Key key = field.getAnnotation(Key.class);
            if (null != local && null != key) {
                if (key.name().equals("hits")) {
                    localMapping.put("local_hits", key.name());
                    localEntityAggColMapping.put("local_hits", TradeStatisticalAnalysisBankFlow.EntityMapping.local_source.name());
                } else {
                    localMapping.put(local.name(), key.name());
                    localEntityAggColMapping.put(local.name(), field.getName());
                }
            }
            Opposite opposite = field.getAnnotation(Opposite.class);
            if (null != opposite && null != key) {
                if (key.name().equals("hits")) {
                    oppositeMapping.put("opposite_hits", key.name());
                    oppositeEntityAggColMapping.put("opposite_hits", TradeStatisticalAnalysisBankFlow.EntityMapping.opposite_source.name());
                } else {
                    oppositeMapping.put(opposite.name(), key.name());
                    oppositeEntityAggColMapping.put(opposite.name(), field.getName());
                }
            }
        });
        localQuery.setMapping(localMapping);
        localQuery.setEntityAggColMapping(localEntityAggColMapping);
        oppositeQuery.setMapping(oppositeMapping);
        oppositeQuery.setEntityAggColMapping(oppositeEntityAggColMapping);
        // 计算总量
        filterTotal = aggregationRequestParamFactory.buildTradeStatisticsAnalysisTotalAgg(queryRequest);
        Map<String, String> totalMap = new HashMap<>();
        totalMap.put("cardinality_total", "value");
        filterTotal.setMapping(totalMap);
        localQuery.addSiblingAggregation(filterTotal);
        List<String> localTitles = new ArrayList<>(localQuery.getMapping().keySet());
        List<String> oppositeTitles = new ArrayList<>(oppositeQuery.getMapping().keySet());

        Map<String, List<List<Object>>> result = entranceRepository.compoundQueryAndAgg(query, localQuery, BankTransactionFlow.class, caseId);

        // 本方实体属性映射
        List<Map<String, Object>> localEntityMapping = FundTacticsAggResultParseFactory.convertEntityMapping(
                FundTacticsAggResultParseFactory.getColValueMapList(result.get(localQuery.getName()), localTitles),
                localQuery.getEntityAggColMapping());
        // 本方实体数据组装
        List<TradeStatisticalAnalysisBankFlow> localResults =
                FundTacticsAggResultParseFactory.getTradeStatisticalAnalysisResult(localEntityMapping);

        List<String> mainCards = localResults.stream().map(TradeStatisticalAnalysisBankFlow::getTradeCard).collect(Collectors.toList());
        List<List<Object>> oppositeList = new ArrayList<>();
        if (CollectionUtils.isEmpty(queryRequest.getCardNums()) || StringUtils.isBlank(queryRequest.getIdentityCard())) {

            // 全部查询条件的时候, 第二个桶的调单卡号集合 是基于第一个桶统计出的调单集合为基础

            if (!CollectionUtils.isEmpty(mainCards)) {

                // 重新设置查询卡号
                queryRequest.setCardNums(mainCards);
                AggregationParams oppositeQueryNew =
                        aggregationRequestParamFactory.buildTradeStatisticsAnalysisOppositeCardAgg(queryRequest);
                QuerySpecialParams queryNew = queryRequestParamFactory.createTradeStatisticalAnalysisQueryRequest(queryRequest, caseId);
                oppositeQueryNew.setMapping(oppositeMapping);
                oppositeQueryNew.setEntityAggColMapping(oppositeEntityAggColMapping);
                Map<String, List<List<Object>>> oppositeResult = entranceRepository.compoundQueryAndAgg(queryNew, oppositeQueryNew, BankTransactionFlow.class, caseId);
                oppositeList = oppositeResult.get(oppositeQueryNew.getName());
            }
        } else {

            oppositeList = result.get(oppositeQuery.getName());
        }
        // 对方实体属性映射
        List<Map<String, Object>> oppositeEntityMapping = FundTacticsAggResultParseFactory.convertEntityMapping(
                FundTacticsAggResultParseFactory.getColValueMapList(oppositeList, oppositeTitles),
                oppositeQuery.getEntityAggColMapping());

        // 对方实体数据组装
        List<TradeStatisticalAnalysisBankFlow> oppositeResults = FundTacticsAggResultParseFactory.getTradeStatisticalAnalysisResult(oppositeEntityMapping);

        // 合并本方 与 对方实体 (并且内存进行分页和排序)
        // 如果分析的是全部,需要对
        localResults.addAll(oppositeResults);

        // 根据交易卡号进行分组
        Map<String, List<TradeStatisticalAnalysisBankFlow>> merge = localResults.stream()
                .collect(Collectors.groupingBy(TradeStatisticalAnalysisBankFlow::getTradeCard));
        // 处理 mergeResult 的value 大于1的情况
        List<TradeStatisticalAnalysisBankFlow> mergeResult = new ArrayList<>();
        merge.forEach((key, value) -> {

            if (value.size() > 1) {
                // 需要合并几个实体的属性
                mergeResult.add(mergeTradeStatisticalAnalysisBankFlow(value));
            } else {
                TradeStatisticalAnalysisBankFlow analysisBankFlow = value.get(0);
                calculationDate(analysisBankFlow, BigDecimalUtil.longValue(analysisBankFlow.getEarliestTradingTime()),
                        BigDecimalUtil.longValue(analysisBankFlow.getLatestTradingTime()));
                mergeResult.addAll(value);
            }
        });

        // 内存排序、分页
        List<TradeStatisticalAnalysisBankFlow> finalResult = sortingAndPageOnMemory(mergeResult, queryRequest.getSortRequest(), queryRequest.getPageRequest());

        TradeStatisticalAnalysisQueryResponse response = new TradeStatisticalAnalysisQueryResponse();
        // 总数量
        long total = (long) result.get("total").get(0).get(0);
        // 每页显示条数
        Integer pageSize = queryRequest.getPageRequest().getPageSize();
        response.setContent(finalResult);
        response.setSize(pageSize);
        response.setTotal(total);
        response.setTotalPages(PageRequest.getTotalPages(total, pageSize));
        return ServerResponse.createBySuccess(response);
    }

    private List<TradeStatisticalAnalysisBankFlow> sortingAndPageOnMemory(List<TradeStatisticalAnalysisBankFlow> list, SortRequest sortRequest,
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

    private TradeStatisticalAnalysisBankFlow mergeTradeStatisticalAnalysisBankFlow(List<TradeStatisticalAnalysisBankFlow> bankFlowList) {

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

    private void calculationDate(TradeStatisticalAnalysisBankFlow bankFlow, long minDate, long maxDate) {
        // 由于es 的时区问题(es 是0时区设置的),需要对最早和最晚日期分别加上 +8小时
        Instant minInstant = Instant.ofEpochMilli(minDate);
        String minTime = LocalDateTime.ofInstant(minInstant, ZoneOffset.ofHours(8)).format(format);
        Instant maxInstant = Instant.ofEpochMilli(maxDate);
        String maxTime = LocalDateTime.ofInstant(maxInstant, ZoneOffset.ofHours(8)).format(format);
        bankFlow.setEarliestTradingTime(minTime);
        bankFlow.setLatestTradingTime(maxTime);
    }
}
