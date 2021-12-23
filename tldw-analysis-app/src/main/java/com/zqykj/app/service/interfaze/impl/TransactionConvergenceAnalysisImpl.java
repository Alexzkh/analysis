/**
 * @作者 Mcj
 */
package com.zqykj.app.service.interfaze.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zqykj.app.service.config.ThreadPoolConfig;
import com.zqykj.app.service.factory.param.agg.TradeConvergenceAnalysisAggParamFactory;
import com.zqykj.app.service.factory.param.query.TradeConvergenceAnalysisQueryParamFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.interfaze.IFundTacticsAnalysis;
import com.zqykj.app.service.interfaze.ITransactionConvergenceAnalysis;
import com.zqykj.app.service.factory.AggregationEntityMappingFactory;
import com.zqykj.app.service.factory.AggregationResultEntityParseFactory;
import com.zqykj.app.service.vo.fund.FundAnalysisResultResponse;
import com.zqykj.app.service.vo.fund.TradeConvergenceAnalysisQueryRequest;
import com.zqykj.app.service.vo.fund.TradeConvergenceAnalysisResult;
import com.zqykj.common.core.ServerResponse;
import com.zqykj.domain.PageRequest;
import com.zqykj.domain.bank.BankTransactionRecord;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.query.QuerySpecialParams;
import com.zqykj.repository.EntranceRepository;
import com.zqykj.util.JacksonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class TransactionConvergenceAnalysisImpl implements ITransactionConvergenceAnalysis {

    private final EntranceRepository entranceRepository;

    private final TradeConvergenceAnalysisAggParamFactory aggregationRequestParamFactory;

    private final TradeConvergenceAnalysisQueryParamFactory queryRequestParamFactory;

    private final AggregationEntityMappingFactory aggregationEntityMappingFactory;

    private final AggregationResultEntityParseFactory aggregationResultEntityParseFactory;

    private final IFundTacticsAnalysis fundTacticsAnalysis;

    @Value("${buckets.page.initSize}")
    private int initGroupSize;

    @Value("${global.chunkSize}")
    private int globalChunkSize;

    @Value("${chunkSize}")
    private int chunkSize;

    private static final String CARDINALITY_TOTAL = "cardinality_total";

    @Override
    public ServerResponse<FundAnalysisResultResponse<TradeConvergenceAnalysisResult>> convergenceAnalysisResult(TradeConvergenceAnalysisQueryRequest request, String caseId) throws ExecutionException, InterruptedException {

        FundAnalysisResultResponse<TradeConvergenceAnalysisResult> resultResponse = new FundAnalysisResultResponse<>();
        Map<String, Object> map;
        if (request.getSearchType() == 0 && !CollectionUtils.isEmpty(request.getCardNums())) {

            com.zqykj.common.vo.PageRequest pageRequest = request.getPageRequest();
            int from = com.zqykj.common.vo.PageRequest.getOffset(pageRequest.getPage(), pageRequest.getPageSize());
            int size = pageRequest.getPageSize();
            // 设置分组桶的大小
            request.setGroupInitSize(initGroupSize);
            map = convergenceAnalysisResultViaChosenMainCards(request, from, size, caseId, true);
        } else {
            // TODO 全部查询,暂定只支持查询到100页,过大不仅消耗内存 且查询速度过慢
            // 全部条件
            if (request.getPageRequest().getPage() > 100) {
                return ServerResponse.createBySuccess("分页上限为100页", FundAnalysisResultResponse.empty());
            }
            map = convergenceAnalysisResultViaAllMainCards(request, caseId);
        }
        List<TradeConvergenceAnalysisResult> results = (List<TradeConvergenceAnalysisResult>) map.get("result");
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
    protected Map<String, Object> convergenceAnalysisResultViaChosenMainCards(TradeConvergenceAnalysisQueryRequest request, int from, int size, String caseId, boolean isComputeTotal) {

        // 构建 交易汇聚分析查询请求
        QuerySpecialParams convergenceQuery = queryRequestParamFactory.buildTradeConvergenceAnalysisResultMainCardsRequest(request, caseId);

        // 构建 交易汇聚分析聚合请求
        AggregationParams convergenceAgg = aggregationRequestParamFactory.buildTradeConvergenceAnalysisResultMainCardsAgg(request, from, size);

        // 构建 mapping (聚合名称 -> 聚合属性)  , (实体属性 -> 聚合名称)
        Map<String, String> aggMapping = new LinkedHashMap<>();
        Map<String, String> entityMapping = new LinkedHashMap<>();
        aggregationEntityMappingFactory.buildTradeAnalysisResultAggMapping(aggMapping, entityMapping, TradeConvergenceAnalysisResult.class);
        convergenceAgg.setMapping(aggMapping);
        convergenceAgg.setEntityAggColMapping(entityMapping);
        convergenceAgg.setResultName("chosen_main_cards");

        Map<String, Object> map = new HashMap<>();
        // 设置同级聚合(计算总数据量) 注: es 计算的去重后数量 是一个近似值
        // 判断是否需要计算总量
        if (isComputeTotal) {
            AggregationParams totalAgg = total(request);
            if (null != totalAgg) {
                convergenceAgg.addSiblingAggregation(totalAgg);
            }
        }
        Map<String, List<List<Object>>> results = entranceRepository.compoundQueryAndAgg(convergenceQuery, convergenceAgg, BankTransactionRecord.class, caseId);
        if (CollectionUtils.isEmpty(results)) {
            map.put("total", 0);
            map.put("result", new ArrayList<>());
            return map;
        }
        // 聚合返回结果
        List<List<Object>> returnResults = results.get(convergenceAgg.getResultName());
        // 一组实体属性集合 与 聚合名称顺序是一一对应的( 所以聚合返回的结果每一列值的属性 与 实体属性也是对应的, 处理聚合展示字段需要特殊处理)
        List<String> entityTitles = new ArrayList<>(entityMapping.keySet());
        // 实体属性值映射
        List<Map<String, Object>> entityPropertyMapping = aggregationResultEntityParseFactory.convertEntity(returnResults, entityTitles, TradeConvergenceAnalysisResult.class);
        // 反序列化实体
        List<TradeConvergenceAnalysisResult> tradeConvergenceAnalysisResults = JacksonUtils.parse(JacksonUtils.toJson(entityPropertyMapping), new TypeReference<List<TradeConvergenceAnalysisResult>>() {
        });
        // 将金额保留2位小数
        tradeConvergenceAnalysisResults.forEach(TradeConvergenceAnalysisResult::amountReservedTwo);
        // 补齐聚合需要展示的字段
        List<String> mergeCards = tradeConvergenceAnalysisResults.stream().map(e -> e.getMergeCardKey()).collect(Collectors.toList());
        List<TradeConvergenceAnalysisResult> tradeConvergenceAnalysisHits = getTradeConvergenceAnalysisHits(mergeCards, caseId);
        addTradeConvergenceAnalysisShowFields(tradeConvergenceAnalysisResults, tradeConvergenceAnalysisHits);

        if (CollectionUtils.isEmpty(results)) {
            map.put("total", 0);
        } else {
            List<List<Object>> total = results.get(CARDINALITY_TOTAL);
            if (CollectionUtils.isEmpty(total)) {

                map.put("total", 0);
            } else {
                map.put("total", total.get(0).get(0));
            }
        }
        map.put("result", tradeConvergenceAnalysisResults);
        return map;
    }

    /**
     * <h2> 通过查询条件过滤获取去重后的调单查询卡号集合 </h2>
     */
    private List<String> getQueryCardsViaDistinctMergeCard(TradeConvergenceAnalysisQueryRequest request, int from, int size, String caseId) {

        // 构建查询请求参数
        QuerySpecialParams query = queryRequestParamFactory.buildTradeConvergenceAnalysisResultMainCardsRequest(request, caseId);
        // 构建 交易汇聚分析聚合请求
        AggregationParams agg = aggregationRequestParamFactory.buildTradeConvergenceQueryAndMergeCardsAgg(request, from, size);
        // 构建 mapping (聚合名称 -> 聚合属性)
        Map<String, String> mapping = aggregationEntityMappingFactory.buildGroupByAggMapping(FundTacticsAnalysisField.MERGE_CARD);
        agg.setMapping(mapping);
        agg.setResultName("queryAndMergeCards");
        Map<String, List<List<Object>>> results = entranceRepository.compoundQueryAndAgg(query, agg, BankTransactionRecord.class, caseId);
        List<List<Object>> cards = results.get(agg.getResultName());
        return cards.stream().map(e -> e.get(0).toString()).collect(Collectors.toList());
    }

    /**
     * <h2> 构建交易汇聚分析结果查询计算总数据量 聚合请求 </h2>
     */
    private AggregationParams total(TradeConvergenceAnalysisQueryRequest request) {

        if (null == request) {
            return null;
        }
        AggregationParams totalAgg = aggregationRequestParamFactory.buildTradeConvergenceAnalysisResultTotalAgg(request);
        totalAgg.setMapping(aggregationEntityMappingFactory.buildDistinctTotalAggMapping(FundTacticsAnalysisField.MERGE_CARD));
        totalAgg.setResultName(CARDINALITY_TOTAL);
        return totalAgg;
    }

    /**
     * <h2> 按照本方查询卡号(即是全部调单卡号进行分析) </h2>
     * <p>
     * 分析的结果: 其中交易卡号出现的必须是调单的(无论它原来是在本方还是对方)
     */
    @SuppressWarnings("all")
    protected Map<String, Object> convergenceAnalysisResultViaAllMainCards(TradeConvergenceAnalysisQueryRequest request, String caseId) throws ExecutionException, InterruptedException {

        // 前台分页
        Map<String, Object> resultMap = new HashMap<>();
        com.zqykj.common.vo.PageRequest pageRequest = request.getPageRequest();
        int page = pageRequest.getPage();
        int pageSize = pageRequest.getPageSize();
        // 异步执行 全部查询任务
        // 获取全部查询的总量
        AggregationParams totalAgg = total(request);
        // 构建 交易汇聚分析查询请求
        QuerySpecialParams convergenceQuery = queryRequestParamFactory.buildTradeConvergenceAnalysisResultMainCardsRequest(request, caseId);
        Map<String, List<List<Object>>> totalResults = entranceRepository.compoundQueryAndAgg(convergenceQuery, totalAgg, BankTransactionRecord.class, caseId);
        long total = 0;
        if (CollectionUtils.isEmpty(totalResults)) {
            resultMap.put("total", 0);
            resultMap.put("result", new ArrayList<>());
            return resultMap;
        } else {
            List<List<Object>> list = totalResults.get(CARDINALITY_TOTAL);
            if (!CollectionUtils.isEmpty(list)) {
                total = (long) list.get(0).get(0);
            }
        }
        // 因为es 计算的去重总量是一个近似值,因此可能总量会少(这里需要调整一下)
        long computeTotal = total + total / 100;
        // 设置分组数量
        request.setGroupInitSize(initGroupSize);
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
        List<String> mergeCards = new ArrayList<>();
        StopWatch stopWatch = StopWatch.createStarted();
        while (position < size) {
            int next = Math.min(position + chunkSize, size);
            Future<List<String>> future = executor.submit(new ConvergenceFutureTask(position,
                    next, skip, limit, caseId, request));
            List<String> results = future.get();
            if (!CollectionUtils.isEmpty(results)) {
                mergeCards.addAll(results);
            }
            if (mergeCards.size() == pageSize) {
                break;
            } else {
                if (mergeCards.size() > 0) {
                    skip = 0;
                    limit = pageSize - mergeCards.size();
                }
                position = next;
            }
        }
        List<TradeConvergenceAnalysisResult> convergenceAnalysisResults = new ArrayList<>();
        // convergenceAnalysisResultAdjustCards 是过滤出的合并卡号集合
        // 获取这些合并卡号集合的聚合分析结果
        if (!CollectionUtils.isEmpty(mergeCards)) {
            request.setMergeCards(mergeCards);
            // 设置分组桶的大小
            request.setGroupInitSize(mergeCards.size());
            Map<String, Object> resultsMap = convergenceAnalysisResultViaChosenMainCards(request, 0, mergeCards.size(), caseId, false);
            convergenceAnalysisResults = (List<TradeConvergenceAnalysisResult>) resultsMap.get("result");
        }
        stopWatch.stop();
        log.info("async compute convergence analysis results cost time = {} ms", stopWatch.getTime(TimeUnit.MILLISECONDS));
        resultMap.put("total", total);
        resultMap.put("result", convergenceAnalysisResults);
        return resultMap;
    }

    /**
     * <h2> 交易汇聚分析异步任务查询类(针对全部查询) </h2>
     */
    class ConvergenceFutureTask implements Callable<List<String>> {

        private int position;

        private int next;

        private String caseId;

        private int skip;

        private int limit;

        private TradeConvergenceAnalysisQueryRequest request;

        public ConvergenceFutureTask(int position, int next, int skip, int limit,
                                     String caseId, TradeConvergenceAnalysisQueryRequest request) {
            this.position = position;
            this.next = next;
            this.skip = skip;
            this.limit = limit;
            this.caseId = caseId;
            this.request = request;
        }

        @Override
        public List<String> call() throws ExecutionException, InterruptedException {

            StopWatch stopWatch = StopWatch.createStarted();
            List<String> mergeCards = asyncQueryConvergenceResultCards(position, next, request, caseId);
            if (CollectionUtils.isEmpty(mergeCards)) {
                return null;
            }
            List<String> adjustCards = mergeCards.stream().
                    map(e -> e.split("-")[0]).collect(Collectors.toList());
            // 过滤出的调单卡号集合
            Map<String, String> filterMainCards = fundTacticsAnalysis.asyncFilterMainCards(caseId, adjustCards);
            if (CollectionUtils.isEmpty(filterMainCards)) {
                return null;
            }
            // 返回最终的合并卡号
            List<String> finalCards = mergeCards.stream()
                    .filter(e -> filterMainCards.containsKey(e.split("-")[0]))
                    .skip(skip).limit(limit).collect(Collectors.toList());
            log.info("Current Thread  = {} ,filter mainCards  cost time = {} ms", Thread.currentThread().getName(), stopWatch.getTime(TimeUnit.MILLISECONDS));
            return finalCards;
        }
    }

    /**
     * <h2> 异步任务查询交易汇聚结果(获取交易卡号、合并卡号集合) </h2>
     */
    private List<String> asyncQueryConvergenceResultCards(int from, int size, TradeConvergenceAnalysisQueryRequest request,
                                                          String caseId) throws ExecutionException, InterruptedException {
        StopWatch stopWatch = StopWatch.createStarted();
        int position = from;
        List<String> cards = new ArrayList<>(size);
        List<CompletableFuture<List<String>>> futures = new ArrayList<>();
        while (position < size) {
            int next = Math.min(position + chunkSize, size);
            int finalPosition = position;
            CompletableFuture<List<String>> future = CompletableFuture.supplyAsync(() ->
                    getCardsViaQueryConvergenceResult(request, finalPosition, chunkSize, caseId), ThreadPoolConfig.getExecutor());
            position = next;
            futures.add(future);
        }
        for (CompletableFuture<List<String>> future : futures) {
            List<String> card = future.get();
            if (!CollectionUtils.isEmpty(card)) {
                cards.addAll(card);
            }
        }
        stopWatch.stop();
        log.info("async batch query merge cards cost time = {}", stopWatch.getTime(TimeUnit.MILLISECONDS));
        return cards;
    }

    /**
     * <h2> 获取交易卡号、合并卡号集合(可能存在调单的、或者非调单的) 通过查询交易汇聚结果 </h2>
     * <p>
     * 查询的表是 {@link BankTransactionRecord}
     */
    private List<String> getCardsViaQueryConvergenceResult(TradeConvergenceAnalysisQueryRequest request, int position, int next,
                                                           String caseId) {

        List<String> convergenceResults = getQueryCardsViaDistinctMergeCard(request, position, next, caseId);
        if (CollectionUtils.isEmpty(convergenceResults)) {
            return null;
        }
        return convergenceResults;
    }

    /**
     * <h2> 获取交易汇聚分析结果中的非统计分析值 </h2>
     * <p>
     * 开户名称、开户证件号码、开户银行、交易卡号、对方开户名称、对方开户证件号码、对方开户银行、对方卡号
     */
    private List<TradeConvergenceAnalysisResult> getTradeConvergenceAnalysisHits(List<String> mergeCards, String caseId) {

        // 构建查询参数
        QuerySpecialParams condition = queryRequestParamFactory.buildTradeConvergenceAnalysisHitsQuery(mergeCards, caseId);
        // 构建聚合参数
        AggregationParams agg = aggregationRequestParamFactory.buildTradeConvergenceAnalysisHitsAgg(mergeCards.size());
        Map<String, String> map = aggregationEntityMappingFactory.buildShowFieldsAggMapping();
        agg.setMapping(map);
        agg.setResultName("hits");
        Map<String, List<List<Object>>> resultMap = entranceRepository.compoundQueryAndAgg(condition, agg, BankTransactionRecord.class, caseId);
        List<Map<String, Object>> results = aggregationResultEntityParseFactory.convertEntity(resultMap.get(agg.getResultName()),
                new ArrayList<>(), TradeConvergenceAnalysisResult.class);
        return JacksonUtils.parse(JacksonUtils.toJson(results), new TypeReference<List<TradeConvergenceAnalysisResult>>() {
        });
    }

    /**
     * <h2> 补充交易汇聚分析结果聚合展示字段 </h2>
     */
    private void addTradeConvergenceAnalysisShowFields(List<TradeConvergenceAnalysisResult> convergenceAnalysisResults,
                                                       List<TradeConvergenceAnalysisResult> hits) {

        for (TradeConvergenceAnalysisResult convergenceAnalysisResult : convergenceAnalysisResults) {

            for (TradeConvergenceAnalysisResult hit : hits) {

                if (convergenceAnalysisResult.getMergeCardKey().equals(hit.getMergeCard())) {
                    // 开户名称
                    convergenceAnalysisResult.setCustomerName(hit.getCustomerName());
                    // 开户证件号码
                    convergenceAnalysisResult.setCustomerIdentityCard(hit.getCustomerIdentityCard());
                    // 开户银行
                    convergenceAnalysisResult.setBank(hit.getBank());
                    // 交易卡号
                    convergenceAnalysisResult.setTradeCard(hit.getTradeCard());
                    // 对方开户名称
                    convergenceAnalysisResult.setOppositeCustomerName(hit.getOppositeCustomerName());
                    // 对方开户证件号码
                    convergenceAnalysisResult.setOppositeIdentityCard(hit.getOppositeIdentityCard());
                    // 对方开户银行
                    convergenceAnalysisResult.setOppositeBank(hit.getOppositeBank());
                    // 对方卡号
                    convergenceAnalysisResult.setOppositeTradeCard(hit.getOppositeTradeCard());
                }
            }
        }
    }
}

