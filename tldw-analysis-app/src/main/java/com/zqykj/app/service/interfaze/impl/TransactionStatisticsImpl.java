package com.zqykj.app.service.interfaze.impl;

import com.fasterxml.jackson.core.type.TypeReference;
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
import com.zqykj.app.service.interfaze.factory.AggregationEntityMappingFactory;
import com.zqykj.app.service.interfaze.factory.AggregationRequestParamFactory;
import com.zqykj.app.service.interfaze.factory.AggregationResultEntityParseFactory;
import com.zqykj.app.service.interfaze.factory.QueryRequestParamFactory;
import com.zqykj.domain.bank.BankTransactionRecord;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.repository.EntranceRepository;
import com.zqykj.util.BigDecimalUtil;
import com.zqykj.util.JacksonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.zqykj.common.vo.TimeTypeRequest;
import com.zqykj.parameters.query.QuerySpecialParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
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

    @Value("${buckets.page.initSize}")
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
        FundAnalysisDateRequest fundAnalysisDateRequest = new FundAnalysisDateRequest();
        BeanUtils.copyProperties(tradeStatisticalAnalysisPreRequest, fundAnalysisDateRequest);
        fundAnalysisDateRequest.setTimeType(timeTypeRequest);
        TradeStatisticalAnalysisFundSumByDate tradeAmountByTime = this.getSummaryOfTradeAmountGroupedByTime(caseId, fundAnalysisDateRequest);

        TimeGroupTradeAmountSum timeGroupTradeAmountSum = new TimeGroupTradeAmountSum();

        timeGroupTradeAmountSum.setDates(tradeAmountByTime.getDates());
        timeGroupTradeAmountSum.setTradeAmounts(tradeAmountByTime.getTradeAmounts());

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
    public TradeStatisticalAnalysisFundSumByDate getSummaryOfTradeAmountGroupedByTime(String caseId, FundAnalysisDateRequest request) {

        // 构建查询参数
        QuerySpecialParams query = this.preQueryTransactionStatisticsAnalysis(caseId, request);

        // 构建日期聚合参数
        AggregationParams dateAgg = aggregationRequestParamFactory.buildTradeStatisticsAnalysisFundByTimeType(request);

        Map<String, String> mapping = new LinkedHashMap<>();

        aggregationEntityMappingFactory.aggNameForMetricsMapping(mapping, TradeStatisticalAnalysisFundSumByDate.class);

        dateAgg.setMapping(mapping);

        dateAgg.setResultName("fundSumByDate");

        // 构建  DateSpecificFormat对象
        List<List<Object>> result = entranceRepository.dateGroupAgg(query, dateAgg, BankTransactionFlow.class, caseId);

        TradeStatisticalAnalysisFundSumByDate groupTradeAmountSum = new TradeStatisticalAnalysisFundSumByDate();

        List<Object> dates = result.get(0);
        List<Object> tradeAmounts = result.get(1);

        if (!CollectionUtils.isEmpty(result)) {
            if (TimeTypeRequest.h == request.getTimeType()) {

                Map<String, BigDecimal> map = new HashMap<>();

                for (int i = 0; i < dates.size(); i++) {

                    String date = dates.get(i).toString();
                    String curTradeAmount = tradeAmounts.get(i).toString();
                    if (map.containsKey(date)) {
                        String OldTradeAmount = map.get(date).toString();
                        map.put(date, BigDecimalUtil.add(OldTradeAmount, curTradeAmount));
                    } else {
                        map.put(date, BigDecimalUtil.value(curTradeAmount));
                    }
                }
                LinkedHashSet<String> sortDates = map.keySet().stream().sorted(Comparator.comparing(Integer::valueOf)).collect(Collectors.toCollection(LinkedHashSet::new));
                groupTradeAmountSum.setDates(sortDates);
                List<BigDecimal> sortTradeAmounts = new ArrayList<>();
                sortDates.forEach(e -> sortTradeAmounts.add(map.get(e)));
                groupTradeAmountSum.setTradeAmounts(sortTradeAmounts);
            } else {
                Set<String> dateSets = dates.stream().map(Object::toString).collect(Collectors.toCollection(LinkedHashSet::new));
                groupTradeAmountSum.setDates(dateSets);
                List<BigDecimal> funds = tradeAmounts.stream().map(x -> BigDecimalUtil.value(x.toString()))
                        .collect(Collectors.toList());
                groupTradeAmountSum.setTradeAmounts(funds);
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
        QuerySpecialParams query;
        // 构建交易统计分析聚合查询
        AggregationParams localAgg;
        AggregationParams oppositeAgg;
        Class<?> indexClass = BankTransactionRecord.class;
        if (!CollectionUtils.isEmpty(queryRequest.getCardNums())) {

            // 这里如果用户明确提供了一组调单卡号集合,此时需要查询表BankTransactionRecord(该表基于现实中交易存储数据,一条交易流水,A手机汇款/收款, B手机收款/汇款)
            localAgg = aggregationRequestParamFactory.buildTradeStatisticsAnalysisByMainCards(queryRequest);
            query = queryRequestParamFactory.createTradeStatisticalAnalysisQueryRequestByMainCards(queryRequest, caseId);
        } else {
            // 本方
            indexClass = BankTransactionFlow.class;
            localAgg = aggregationRequestParamFactory.buildTradeStatisticsAnalysisQueryCardAgg(queryRequest);
            query = queryRequestParamFactory.createTradeStatisticalAnalysisQueryRequest(queryRequest, caseId);
        }
        oppositeAgg = new AggregationParams();
        Map<String, Map<String, String>> aggKeyMapping = new LinkedHashMap<>();
        Map<String, Map<String, String>> entityAggKeyMapping = new LinkedHashMap<>();
        // 获取聚合Key 映射 , 聚合key 与 返回实体属性 映射
        aggregationEntityMappingFactory.entityAggMetricsMappingOfLocalOpposite(aggKeyMapping, entityAggKeyMapping, TradeStatisticalAnalysisBankFlow.class);
        Map<String, String> localMapping = aggKeyMapping.get("localMapping");
        Map<String, String> localEntityAggColMapping = entityAggKeyMapping.get("localEntityAggColMapping");
        localAgg.setMapping(localMapping);
        localAgg.setEntityAggColMapping(localEntityAggColMapping);
        localAgg.setResultName("local");
        // 计算总量
        AggregationParams filterTotal = aggregationRequestParamFactory.buildTradeStatisticsAnalysisTotalAgg(queryRequest);
        Map<String, String> totalMap = new HashMap<>();
        totalMap.put("cardinality_total", "value");
        filterTotal.setMapping(totalMap);
        filterTotal.setResultName("total");
        localAgg.addSiblingAggregation(filterTotal);

        if (!CollectionUtils.isEmpty(queryRequest.getCardNums())) {

            localMapping.put("local_min_date", "valueAsString");
            localMapping.put("local_max_date", "valueAsString");
        }
        Map<String, List<List<Object>>> result = entranceRepository.compoundQueryAndAgg(query, localAgg, indexClass, caseId);

        List<String> localTitles = new ArrayList<>(localAgg.getMapping().keySet());
        // 本方实体属性映射
        List<Map<String, Object>> localEntityMapping = aggregationResultEntityParseFactory.convertEntity(
                aggregationResultEntityParseFactory.getColValueMapList(result.get(localAgg.getResultName()), localTitles),
                localAgg.getEntityAggColMapping());
        // 本方实体数据组装
        List<TradeStatisticalAnalysisBankFlow> localResults = JacksonUtils.parse(JacksonUtils.toJson(localEntityMapping), new TypeReference<List<TradeStatisticalAnalysisBankFlow>>() {
        });
        // 第一个桶聚合返回的调单卡号集合
        List<String> mainCards = localResults.stream().map(TradeStatisticalAnalysisBankFlow::getTradeCard).collect(Collectors.toList());
        // 最终数据返回
        List<TradeStatisticalAnalysisBankFlow> finalResult = new ArrayList<>();
        // 查询条件为全部的时候,需要计算 对方卡号存在 调单的情况
        if (queryRequest.getSearchType() == 1 || CollectionUtils.isEmpty(queryRequest.getCardNums())) {

            // 全部查询条件的时候, 第二个桶的调单卡号集合 是基于第一个桶统计出的调单集合为基础
            // 因为第二个桶统计的是调档再对方卡号中的情况, 相当于 对方卡号 在所有的本方查询卡号之内的情况
            if (!CollectionUtils.isEmpty(mainCards)) {
                Map<String, String> oppositeMapping = aggKeyMapping.get("oppositeMapping");
                Map<String, String> oppositeEntityAggColMapping = entityAggKeyMapping.get("oppositeEntityAggColMapping");
                oppositeAgg.setMapping(oppositeMapping);
                oppositeAgg.setEntityAggColMapping(oppositeEntityAggColMapping);
                oppositeAgg.setResultName("opposite");
                List<String> oppositeTitles = new ArrayList<>(oppositeAgg.getMapping().keySet());
                // 重新设置查询卡号
                queryRequest.setSearchTag("opposite");
                queryRequest.setCardNums(mainCards);
                AggregationParams oppositeQueryNew =
                        aggregationRequestParamFactory.buildTradeStatisticsAnalysisOppositeCardAgg(queryRequest);
                QuerySpecialParams queryNew = queryRequestParamFactory.createTradeStatisticalAnalysisQueryRequest(queryRequest, caseId);
                oppositeQueryNew.setMapping(oppositeMapping);
                oppositeQueryNew.setEntityAggColMapping(oppositeEntityAggColMapping);
                oppositeQueryNew.setResultName("opposite");
                Map<String, List<List<Object>>> oppositeResult = entranceRepository.compoundQueryAndAgg(queryNew, oppositeQueryNew, indexClass, caseId);
                List<List<Object>> oppositeList = oppositeResult.get(oppositeQueryNew.getResultName());
                // 对方实体属性映射
                List<Map<String, Object>> oppositeEntityMapping = aggregationResultEntityParseFactory.convertEntity(
                        aggregationResultEntityParseFactory.getColValueMapList(oppositeList, oppositeTitles),
                        oppositeAgg.getEntityAggColMapping());

                // 对方实体数据组装
                List<TradeStatisticalAnalysisBankFlow> oppositeResults = JacksonUtils.parse(JacksonUtils.toJson(oppositeEntityMapping), new TypeReference<List<TradeStatisticalAnalysisBankFlow>>() {
                });
                if (!CollectionUtils.isEmpty(oppositeResults)) {
                    // 如果分析的是全部,需要合并本方 与 对方实体 (并且内存进行分页和排序)
                    localResults.addAll(oppositeResults);
                }

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
                        TradeStatisticalAnalysisBankFlow.amountReservedTwo(analysisBankFlow);
                        mergeResult.addAll(value);
                    }
                });
                // 内存排序、分页
                finalResult = TradeStatisticalAnalysisBankFlow.sortingAndPageOnMemory(mergeResult, queryRequest.getSortRequest(), queryRequest.getPageRequest());
            }
        } else {
            finalResult = localResults;
            // 处理日期
            // 金额自动保留2位
            finalResult.forEach(TradeStatisticalAnalysisBankFlow::amountReservedTwo);
        }
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
