package com.zqykj.app.service.interfaze.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zqykj.app.service.config.ThreadPoolConfig;
import com.zqykj.app.service.interfaze.IFundTacticsAnalysis;
import com.zqykj.app.service.interfaze.ITransactionStatistics;
import com.zqykj.app.service.transform.NumericalConversion;
import com.zqykj.app.service.vo.fund.*;
import com.zqykj.common.constant.Constants;
import com.zqykj.common.core.ServerResponse;
import com.zqykj.common.enums.HistogramStatistic;
import com.zqykj.common.request.TradeStatisticalAnalysisPreRequest;
import com.zqykj.common.request.TransactionStatisticsDetailRequest;
import com.zqykj.common.response.*;
import com.zqykj.app.service.vo.fund.FundTacticsPartGeneralPreRequest;
import com.zqykj.common.request.TransactionStatisticsAggs;
import com.zqykj.common.request.TransactionStatisticsRequest;
import com.zqykj.domain.*;
import com.zqykj.domain.bank.BankTransactionFlow;
import com.zqykj.app.service.factory.AggregationEntityMappingFactory;
import com.zqykj.app.service.factory.AggregationRequestParamFactory;
import com.zqykj.app.service.factory.AggregationResultEntityParseFactory;
import com.zqykj.app.service.factory.QueryRequestParamFactory;
import com.zqykj.domain.bank.BankTransactionRecord;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.repository.EntranceRepository;
import com.zqykj.util.BigDecimalUtil;
import com.zqykj.util.JacksonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.zqykj.common.vo.TimeTypeRequest;
import com.zqykj.parameters.query.QuerySpecialParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TransactionStatisticsImpl implements ITransactionStatistics {


    private final EntranceRepository entranceRepository;

    private final AggregationRequestParamFactory aggregationRequestParamFactory;

    private final QueryRequestParamFactory queryRequestParamFactory;

    private final AggregationEntityMappingFactory aggregationEntityMappingFactory;

    private final AggregationResultEntityParseFactory aggregationResultEntityParseFactory;

    private final IFundTacticsAnalysis fundTacticsAnalysis;

    @Value("${buckets.page.initSize}")
    private int initGroupSize;

    private static final String CARDINALITY_TOTAL = "cardinality_total";

    @Value("${global.chunkSize}")
    private int globalChunkSize;

    @Value("${chunkSize}")
    private int chunkSize;


    @Override
    public TransactionStatisticsResponse calculateStatisticalResults(String caseId, TransactionStatisticsRequest transactionStatisticsRequest) {
        TradeStatisticalAnalysisPreRequest tradeStatisticalAnalysisPreRequest = transactionStatisticsRequest.getTradeStatisticalAnalysisPreRequest();
        TransactionStatisticsAggs transactionStatisticsAggs = transactionStatisticsRequest.getTransactionStatisticsAggs();
        TimeTypeRequest timeTypeRequest = transactionStatisticsAggs.getDateType();

        /**
         * 获取交易金额聚合统计直方图结果.
         * */
        FundTacticsPartGeneralPreRequest tacticsPartGeneralPreRequest = new FundTacticsPartGeneralPreRequest();
        BeanUtils.copyProperties(tradeStatisticalAnalysisPreRequest, tacticsPartGeneralPreRequest);
        HistogramStatisticResponse histogramStatisticResponse = this.getHistogramStatistics(caseId, tacticsPartGeneralPreRequest, transactionStatisticsAggs);

        /**
         * 获取日期折现图聚合统计结果.
         * */
        FundDateRequest fundAnalysisDateRequest = new FundDateRequest();
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
    public HistogramStatisticResponse getHistogramStatistics(String caseId, FundTacticsPartGeneralPreRequest request, TransactionStatisticsAggs transactionStatisticsAggs) {
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
    public QuerySpecialParams preQueryTransactionStatisticsAnalysis(String caseId, FundTacticsPartGeneralPreRequest request) {
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
    public TradeStatisticalAnalysisFundSumByDate getSummaryOfTradeAmountGroupedByTime(String caseId, FundDateRequest request) {

        // 构建查询参数
        QuerySpecialParams query = this.preQueryTransactionStatisticsAnalysis(caseId, request);

        // 构建日期聚合参数
        AggregationParams dateAgg = aggregationRequestParamFactory.buildTradeStatisticsAnalysisFundByTimeType(request);

        Map<String, String> mapping = new LinkedHashMap<>();

        aggregationEntityMappingFactory.buildTradeAnalysisResultAggMapping(mapping, TradeStatisticalAnalysisFundSumByDate.class);

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

    public ServerResponse<FundAnalysisResultResponse<TradeStatisticalAnalysisResult>> getTransactionStatisticsAnalysisResult(String caseId, TradeStatisticalAnalysisQueryRequest request) throws ExecutionException, InterruptedException {

        FundAnalysisResultResponse<TradeStatisticalAnalysisResult> resultResponse = new FundAnalysisResultResponse<>();
        Map<String, Object> map;
        if (request.getSearchType() == 0 && !CollectionUtils.isEmpty(request.getCardNums())) {

            com.zqykj.common.vo.PageRequest pageRequest = request.getPageRequest();
            int from = com.zqykj.common.vo.PageRequest.getOffset(pageRequest.getPage(), pageRequest.getPageSize());
            int size = pageRequest.getPageSize();
            map = statisticsAnalysisResultViaChosenMainCards(request, from, size, caseId, true);
        } else {
            // TODO  全部查询,暂定只支持查询到30页,过大不仅消耗内存 且查询速度过慢
            // 全部条件
            if (request.getPageRequest().getPage() > 30) {
                return ServerResponse.createBySuccess("分页上限为30页", FundAnalysisResultResponse.empty());
            }
            map = statisticsAnalysisResultViaAllMainCards(request, caseId);
        }
        List<TradeStatisticalAnalysisResult> results = (List<TradeStatisticalAnalysisResult>) map.get("result");
        long total = (long) map.get("total");
        Integer pageSize = request.getPageRequest().getPageSize();
        // 结果集
        resultResponse.setContent(results);
        // 每页显示条数
        resultResponse.setSize(pageSize);
        // 总数据量
        resultResponse.setTotal(total);
        // 总页数
        resultResponse.setTotalPages(PageRequest.getTotalPages(total, pageSize));
        return ServerResponse.createBySuccess(resultResponse);
    }

    /**
     * <h2> 选择个体 / 选择部分调单卡号集合 </h2>
     */
    @SuppressWarnings("all")
    private Map<String, Object> statisticsAnalysisResultViaChosenMainCards(TradeStatisticalAnalysisQueryRequest request, int from, int size, String caseId, boolean isComputeTotal) {

        // 设置分组桶的大小
        request.setGroupInitSize(initGroupSize);

        // 构建 交易统计分析查询请求
        QuerySpecialParams tradeStatisticsQuery = queryRequestParamFactory.createTradeStatisticalAnalysisQueryRequestByMainCards(request, caseId, BankTransactionRecord.class);
        // 构建 交易统计分析聚合查询请求
        AggregationParams tradeStatisticsAgg = aggregationRequestParamFactory.buildTradeStatisticsAnalysisByMainCards(request, from, size);

        // 构建 mapping (聚合名称 -> 聚合属性)  , (实体属性 -> 聚合名称)
        Map<String, String> aggMapping = new LinkedHashMap<>();
        Map<String, String> entityMapping = new LinkedHashMap<>();
        aggregationEntityMappingFactory.buildTradeAnalysisResultAggMapping(aggMapping, entityMapping, TradeStatisticalAnalysisResult.class);
        tradeStatisticsAgg.setMapping(aggMapping);
        tradeStatisticsAgg.setEntityAggColMapping(entityMapping);
        tradeStatisticsAgg.setResultName("chosen_main_cards");

        // 设置同级聚合(计算总数据量)
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, List<List<Object>>> totalResults = null;
        if (isComputeTotal) {
            AggregationParams totalAgg = total(request);
            // 获取交易统计查询结果总量
            QuerySpecialParams totalQuery = queryRequestParamFactory.createTradeStatisticalAnalysisQueryRequestByMainCards(request, caseId, BankTransactionFlow.class);
            totalResults = entranceRepository.compoundQueryAndAgg(totalQuery, totalAgg, BankTransactionFlow.class, caseId);
        }
        // 获取交易统计查询结果
        Map<String, List<List<Object>>> results = entranceRepository.compoundQueryAndAgg(tradeStatisticsQuery, tradeStatisticsAgg, BankTransactionRecord.class, caseId);
        if (CollectionUtils.isEmpty(results)) {
            resultMap.put("total", 0);
            resultMap.put("result", new ArrayList<>());
            return resultMap;
        }
        // 聚合返回结果
        List<List<Object>> returnResults = results.get(tradeStatisticsAgg.getResultName());

        // 一组实体属性集合 与 聚合名称顺序是一一对应的( 所以聚合返回的结果每一列值的属性 与 实体属性也是对应的, 处理聚合展示字段需要特殊处理)
        List<String> entityTitles = new ArrayList<>(entityMapping.keySet());

        // 实体属性值映射
        List<Map<String, Object>> entityPropertyMapping = aggregationResultEntityParseFactory.convertEntity(returnResults, entityTitles, TradeStatisticalAnalysisResult.class);

        // 反序列化实体
        List<TradeStatisticalAnalysisResult> tradeStatisticalAnalysisResults = JacksonUtils.parse(JacksonUtils.toJson(entityPropertyMapping), new TypeReference<List<TradeStatisticalAnalysisResult>>() {
        });

        // 将金额保留2位小数
        tradeStatisticalAnalysisResults.forEach(TradeStatisticalAnalysisResult::amountReservedTwo);

        if (CollectionUtils.isEmpty(totalResults)) {
            resultMap.put("total", 0);
        } else {
            List<List<Object>> total = totalResults.get(CARDINALITY_TOTAL);
            if (CollectionUtils.isEmpty(total)) {
                resultMap.put("total", 0);
            } else {
                resultMap.put("total", total.get(0).get(0));
            }
        }
        resultMap.put("result", tradeStatisticalAnalysisResults);
        return resultMap;
    }

    /**
     * <h2> 获取交易统计分析结果总数据量 </h2>
     */
    private AggregationParams total(TradeStatisticalAnalysisQueryRequest request) {

        if (null == request) {
            return null;
        }
        AggregationParams totalAgg = aggregationRequestParamFactory.buildTradeStatisticsAnalysisTotalAgg(request);
        totalAgg.setMapping(aggregationEntityMappingFactory.buildFundTacticsAnalysisResultTotalAggMapping());
        totalAgg.setResultName(CARDINALITY_TOTAL);
        return totalAgg;
    }

    /**
     * <h2> 按照本方查询卡号(即是全部调单卡号进行分析) </h2>
     * <p>
     * 分析的结果: 其中交易卡号出现的必须是调单的(无论它原来是在本方还是对方)
     */
    protected Map<String, Object> statisticsAnalysisResultViaAllMainCards(TradeStatisticalAnalysisQueryRequest request, String caseId) throws ExecutionException, InterruptedException {

        // 前台分页
        Map<String, Object> resultMap = new HashMap<>();
        com.zqykj.common.vo.PageRequest pageRequest = request.getPageRequest();
        int page = pageRequest.getPage();
        int pageSize = pageRequest.getPageSize();
        // 异步执行 全部查询任务
        // 获取全部查询的总量
        AggregationParams totalAgg = total(request);
        // 构建 交易统计分析查询请求
        QuerySpecialParams statisticsQuery = queryRequestParamFactory.createTradeStatisticalAnalysisQueryRequestByMainCards(request, caseId, BankTransactionFlow.class);
        Map<String, List<List<Object>>> totalResults = entranceRepository.compoundQueryAndAgg(statisticsQuery, totalAgg, BankTransactionFlow.class, caseId);
        long total = 0;
        if (!CollectionUtils.isEmpty(totalResults)) {
            List<List<Object>> list = totalResults.get(CARDINALITY_TOTAL);
            if (!CollectionUtils.isEmpty(list)) {
                total = (long) list.get(0).get(0);
            }
        } else {
            resultMap.put("total", 0);
            resultMap.put("result", new ArrayList<>());
            return resultMap;
        }
        // 因为es 计算的去重总量是一个近似值,因此可能总量会少(这里需要调整一下)
        long computeTotal = total + total / 10;
        // 异步任务查询起始位置
        int position = 0;
        // 异步任务查询总量
        int size = Integer.parseInt(String.valueOf(computeTotal));
        // 异步任务查询批处理阈值
        int chunkSize = globalChunkSize;
        ThreadPoolTaskExecutor executor = ThreadPoolConfig.getExecutor();
        // 需要返回的数量
        int skip = com.zqykj.common.vo.PageRequest.getOffset(page, pageSize);
        int limit = pageSize;
        List<TradeStatisticalAnalysisResult> statisticalAnalysisResults = new ArrayList<>();
        StopWatch stopWatch = StopWatch.createStarted();
        while (position < size) {
            int next = Math.min(position + chunkSize, size);
            Future<List<TradeStatisticalAnalysisResult>> future = executor.submit(new StatisticalFutureTask(position,
                    chunkSize, skip, limit, caseId, request));
            List<TradeStatisticalAnalysisResult> results = future.get();
            if (!CollectionUtils.isEmpty(results)) {
                statisticalAnalysisResults.addAll(results);
            }
            if (statisticalAnalysisResults.size() == pageSize) {
                break;
            } else {
                if (statisticalAnalysisResults.size() > 0) {
                    skip = 0;
                    limit = pageSize - statisticalAnalysisResults.size();
                }
                position = next;
            }
        }
        stopWatch.stop();
        log.info("async compute statistical analysis results cost time = {} ms", stopWatch.getTime(TimeUnit.MILLISECONDS));
        resultMap.put("total", total);
        resultMap.put("result", statisticalAnalysisResults);
        return resultMap;
    }

    /**
     * <h2> 交易统计分析异步任务查询类(针对全部查询) </h2>
     */
    class StatisticalFutureTask implements Callable<List<TradeStatisticalAnalysisResult>> {

        private int position;

        private int next;

        private String caseId;

        private int skip;

        private int limit;

        private TradeStatisticalAnalysisQueryRequest request;

        public StatisticalFutureTask(int position, int next, int skip, int limit,
                                     String caseId, TradeStatisticalAnalysisQueryRequest request) {
            this.position = position;
            this.next = next;
            this.skip = skip;
            this.limit = limit;
            this.caseId = caseId;
            this.request = request;
        }

        @Override
        public List<TradeStatisticalAnalysisResult> call() throws ExecutionException, InterruptedException {

            StopWatch stopWatch = StopWatch.createStarted();
            List<TradeStatisticalAnalysisResult> statisticalResults = asyncQueryStatisticalResult(position, next, request, caseId);
            List<String> cards = statisticalResults.stream().map(TradeStatisticalAnalysisResult::getTradeCard).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(cards)) {
                return null;
            }
            // 过滤出的调单卡号集合
            Map<String, String> filterMainCards = fundTacticsAnalysis.asyncFilterMainCards(caseId, cards);
            // 返回最终的调单数据
            List<TradeStatisticalAnalysisResult> finalCards = statisticalResults.stream().filter(e -> filterMainCards.containsKey(e.getTradeCard()))
                    .skip(skip).limit(limit).collect(Collectors.toList());
            log.info("Current Thread  = {} ,filter mainCards  cost time = {} ms", Thread.currentThread().getName(), stopWatch.getTime(TimeUnit.MILLISECONDS));
            return finalCards;
        }
    }

    /**
     * <h2> 异步任务查询交易统计分析结果(获取交易卡号集合) </h2>
     */
    private List<TradeStatisticalAnalysisResult> asyncQueryStatisticalResult(int from, int size, TradeStatisticalAnalysisQueryRequest request,
                                                                             String caseId) throws ExecutionException, InterruptedException {
        int position = from;
        List<TradeStatisticalAnalysisResult> cards = new ArrayList<>(size);
        List<CompletableFuture<List<TradeStatisticalAnalysisResult>>> futures = new ArrayList<>();
        while (position < size) {
            int next = Math.min(position + chunkSize, size);
            int finalPosition = position;
            CompletableFuture<List<TradeStatisticalAnalysisResult>> future = CompletableFuture.supplyAsync(() ->
                    getCardsViaQueryStatisticalResult(request, finalPosition, chunkSize, caseId), ThreadPoolConfig.getExecutor());
            position = next;
            futures.add(future);
        }
        for (CompletableFuture<List<TradeStatisticalAnalysisResult>> future : futures) {
            List<TradeStatisticalAnalysisResult> card = future.get();
            if (!CollectionUtils.isEmpty(card)) {
                cards.addAll(card);
            }
        }
        return cards;
    }

    /**
     * <h2> 获取交易卡号集合(可能存在调单的、或者非调单的) 通过查询交易统计分析结果 </h2>
     * <p>
     * 查询的表是 {@link BankTransactionRecord}
     */
    private List<TradeStatisticalAnalysisResult> getCardsViaQueryStatisticalResult(TradeStatisticalAnalysisQueryRequest request, int position, int next,
                                                                                   String caseId) {

        Map<String, Object> map = statisticsAnalysisResultViaChosenMainCards(request, position, next, caseId, false);
        if (CollectionUtils.isEmpty(map)) {
            return null;
        }
        Object result = map.get("result");
        List<TradeStatisticalAnalysisResult> statisticalResults = (List<TradeStatisticalAnalysisResult>) result;
        if (CollectionUtils.isEmpty(statisticalResults)) {
            return null;
        }
        return statisticalResults;
    }
}
