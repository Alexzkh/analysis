package com.zqykj.app.service.interfaze.impl;

import com.fasterxml.jackson.core.type.TypeReference;
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
import com.zqykj.domain.*;
import com.zqykj.domain.bank.BankTransactionFlow;
import com.zqykj.factory.AggregationEntityMappingFactory;
import com.zqykj.factory.AggregationRequestParamFactory;
import com.zqykj.factory.AggregationResultEntityParseFactory;
import com.zqykj.factory.QueryRequestParamFactory;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.repository.EntranceRepository;
import com.zqykj.util.BigDecimalUtil;
import com.zqykj.util.JacksonUtils;
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

    private final AggregationEntityMappingFactory aggregationEntityMappingFactory;

    private final AggregationResultEntityParseFactory aggregationResultEntityParseFactory;

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
        Map<String, Map<String, String>> aggKeyMapping = new LinkedHashMap<>();
        Map<String, Map<String, String>> entityAggKeyMapping = new LinkedHashMap<>();
        // 获取聚合Key 映射 , 聚合key 与 返回实体属性 映射
        aggregationEntityMappingFactory.entityAggColMapping(aggKeyMapping, entityAggKeyMapping, TradeStatisticalAnalysisBankFlow.class);
        Map<String, String> localMapping = aggKeyMapping.get("localMapping");
        Map<String, String> oppositeMapping = aggKeyMapping.get("oppositeMapping");
        Map<String, String> localEntityAggColMapping = entityAggKeyMapping.get("localEntityAggColMapping");
        Map<String, String> oppositeEntityAggColMapping = entityAggKeyMapping.get("oppositeEntityAggColMapping");

        localQuery.setMapping(localMapping);
        oppositeQuery.setMapping(oppositeMapping);
        localQuery.setEntityAggColMapping(localEntityAggColMapping);
        oppositeQuery.setEntityAggColMapping(oppositeEntityAggColMapping);
        // 计算总量
        filterTotal = aggregationRequestParamFactory.buildTradeStatisticsAnalysisTotalAgg(queryRequest);
        Map<String, String> totalMap = new HashMap<>();
        totalMap.put("cardinality_total", "value");
        filterTotal.setMapping(totalMap);
        localQuery.addSiblingAggregation(filterTotal);

        Map<String, List<List<Object>>> result = entranceRepository.compoundQueryAndAgg(query, localQuery, BankTransactionFlow.class, caseId);

        List<String> localTitles = new ArrayList<>(localQuery.getMapping().keySet());
        List<String> oppositeTitles = new ArrayList<>(oppositeQuery.getMapping().keySet());
        // 本方实体属性映射
        List<Map<String, Object>> localEntityMapping = aggregationResultEntityParseFactory.convertEntity(
                aggregationResultEntityParseFactory.getColValueMapList(result.get(localQuery.getName()), localTitles),
                localQuery.getEntityAggColMapping());
        // 本方实体数据组装
        List<TradeStatisticalAnalysisBankFlow> localResults = JacksonUtils.parse(JacksonUtils.toJson(localEntityMapping), new TypeReference<List<TradeStatisticalAnalysisBankFlow>>() {
        });

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
        List<Map<String, Object>> oppositeEntityMapping = aggregationResultEntityParseFactory.convertEntity(
                aggregationResultEntityParseFactory.getColValueMapList(oppositeList, oppositeTitles),
                oppositeQuery.getEntityAggColMapping());

        // 对方实体数据组装
        List<TradeStatisticalAnalysisBankFlow> oppositeResults = JacksonUtils.parse(JacksonUtils.toJson(oppositeEntityMapping), new TypeReference<List<TradeStatisticalAnalysisBankFlow>>() {
        });
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
                mergeResult.add(TradeStatisticalAnalysisBankFlow.mergeTradeStatisticalAnalysisBankFlow(value));
            } else {
                TradeStatisticalAnalysisBankFlow analysisBankFlow = value.get(0);
                TradeStatisticalAnalysisBankFlow.calculationDate(analysisBankFlow, BigDecimalUtil.longValue(analysisBankFlow.getEarliestTradingTime()),
                        BigDecimalUtil.longValue(analysisBankFlow.getLatestTradingTime()));
                mergeResult.addAll(value);
            }
        });
        // 内存排序、分页
        List<TradeStatisticalAnalysisBankFlow> finalResult = TradeStatisticalAnalysisBankFlow.sortingAndPageOnMemory(mergeResult, queryRequest.getSortRequest(), queryRequest.getPageRequest());
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
}
