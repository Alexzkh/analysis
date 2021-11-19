/**
 * @作者 Mcj
 */
package com.zqykj.app.service.interfaze.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zqykj.app.service.config.ThreadPoolConfig;
import com.zqykj.app.service.interfaze.IFundTacticsAnalysis;
import com.zqykj.app.service.interfaze.ITransactionConvergenceAnalysis;
import com.zqykj.app.service.factory.AggregationEntityMappingFactory;
import com.zqykj.app.service.factory.AggregationRequestParamFactory;
import com.zqykj.app.service.factory.AggregationResultEntityParseFactory;
import com.zqykj.app.service.factory.QueryRequestParamFactory;
import com.zqykj.app.service.vo.fund.TradeConvergenceAnalysisResultResponse;
import com.zqykj.app.service.vo.fund.TradeConvergenceAnalysisQueryRequest;
import com.zqykj.app.service.vo.fund.TradeConvergenceAnalysisResult;
import com.zqykj.app.service.vo.fund.TradeStatisticalAnalysisResult;
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

    private final AggregationRequestParamFactory aggregationRequestParamFactory;

    private final QueryRequestParamFactory queryRequestParamFactory;

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
    public ServerResponse<TradeConvergenceAnalysisResultResponse> convergenceAnalysisResult(TradeConvergenceAnalysisQueryRequest request, String caseId) throws ExecutionException, InterruptedException {

        TradeConvergenceAnalysisResultResponse resultResponse = new TradeConvergenceAnalysisResultResponse();
        Map<String, Object> map;
        if (request.getSearchType() == 0 && !CollectionUtils.isEmpty(request.getCardNums())) {

            com.zqykj.common.vo.PageRequest pageRequest = request.getPageRequest();
            int from = com.zqykj.common.vo.PageRequest.getOffset(pageRequest.getPage(), pageRequest.getPageSize());
            int size = pageRequest.getPageSize();
            map = convergenceAnalysisResultViaChosenMainCards(request, from, size, caseId, true);
        } else {
            // TODO 全部查询,暂定只支持查询到30页,过大不仅消耗内存 且查询速度过慢
            // 全部条件
            if (request.getPageRequest().getPage() > 30) {
                return ServerResponse.createBySuccess("分页上限为30页", new TradeConvergenceAnalysisResultResponse());
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

        // 设置分组桶的大小
        request.setGroupInitSize(initGroupSize);

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
     * <h2> 构建交易汇聚分析结果查询计算总数据量 聚合请求 </h2>
     */
    private AggregationParams total(TradeConvergenceAnalysisQueryRequest request) {

        if (null == request) {
            return null;
        }
        AggregationParams totalAgg = aggregationRequestParamFactory.buildTradeConvergenceAnalysisResultTotalAgg(request);
        totalAgg.setMapping(aggregationEntityMappingFactory.buildFundTacticsAnalysisResultTotalAggMapping());
        totalAgg.setResultName(CARDINALITY_TOTAL);
        return totalAgg;
    }

    /**
     * <h2> 按照本方查询卡号(即是全部调单卡号进行分析) </h2>
     * <p>
     * 分析的结果: 其中交易卡号出现的必须是调单的(无论它原来是在本方还是对方)
     */
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
        List<TradeConvergenceAnalysisResult> convergenceAnalysisResults = new ArrayList<>();
        StopWatch stopWatch = StopWatch.createStarted();
        while (position < size) {
            int next = Math.min(position + chunkSize, size);
            Future<List<TradeConvergenceAnalysisResult>> future = executor.submit(new ConvergenceFutureTask(position,
                    chunkSize, skip, limit, caseId, request));
            List<TradeConvergenceAnalysisResult> results = future.get();
            convergenceAnalysisResults.addAll(results);
            if (convergenceAnalysisResults.size() == pageSize) {
                break;
            } else {
                if (convergenceAnalysisResults.size() > 0) {
                    skip = 0;
                    limit = pageSize - convergenceAnalysisResults.size();
                }
                position = next;
            }
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
    class ConvergenceFutureTask implements Callable<List<TradeConvergenceAnalysisResult>> {

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
        public List<TradeConvergenceAnalysisResult> call() throws ExecutionException, InterruptedException {

            StopWatch stopWatch = StopWatch.createStarted();
            List<TradeConvergenceAnalysisResult> convergenceResults = asyncQueryConvergenceResult(position, next, request, caseId);
            List<String> cards = convergenceResults.stream().map(TradeStatisticalAnalysisResult::getTradeCard).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(cards)) {
                return null;
            }
            // 过滤出的调单卡号集合
            Map<String, String> filterMainCards = fundTacticsAnalysis.asyncFilterMainCards(caseId, cards);
            if (CollectionUtils.isEmpty(filterMainCards)) {
                return null;
            }
            // 返回最终的调单数据
            List<TradeConvergenceAnalysisResult> finalCards = convergenceResults.stream().filter(e -> filterMainCards.containsKey(e.getTradeCard()))
                    .skip(skip).limit(limit).collect(Collectors.toList());
            log.info("Current Thread  = {} ,filter mainCards  cost time = {} ms", Thread.currentThread().getName(), stopWatch.getTime(TimeUnit.MILLISECONDS));
            return finalCards;
        }
    }

    /**
     * <h2> 异步任务查询交易汇聚结果(获取交易卡号集合) </h2>
     */
    private List<TradeConvergenceAnalysisResult> asyncQueryConvergenceResult(int from, int size, TradeConvergenceAnalysisQueryRequest request,
                                                                             String caseId) throws ExecutionException, InterruptedException {
        int position = from;
        List<TradeConvergenceAnalysisResult> cards = new ArrayList<>(size);
        List<CompletableFuture<List<TradeConvergenceAnalysisResult>>> futures = new ArrayList<>();
        while (position < size) {
            int next = Math.min(position + chunkSize, size);
            int finalPosition = position;
            CompletableFuture<List<TradeConvergenceAnalysisResult>> future = CompletableFuture.supplyAsync(() ->
                    getCardsViaQueryConvergenceResult(request, finalPosition, chunkSize, caseId), ThreadPoolConfig.getExecutor());
            position = next;
            futures.add(future);
        }
        for (CompletableFuture<List<TradeConvergenceAnalysisResult>> future : futures) {
            List<TradeConvergenceAnalysisResult> card = future.get();
            if (!CollectionUtils.isEmpty(card)) {
                cards.addAll(card);
            }
        }
        return cards;
    }

    /**
     * <h2> 获取交易卡号集合(可能存在调单的、或者非调单的) 通过查询交易汇聚结果 </h2>
     * <p>
     * 查询的表是 {@link BankTransactionRecord}
     */
    private List<TradeConvergenceAnalysisResult> getCardsViaQueryConvergenceResult(TradeConvergenceAnalysisQueryRequest request, int position, int next,
                                                                                   String caseId) {

        Map<String, Object> map = convergenceAnalysisResultViaChosenMainCards(request, position, next, caseId, false);
        if (CollectionUtils.isEmpty(map)) {
            return null;
        }
        Object result = map.get("result");
        List<TradeConvergenceAnalysisResult> convergenceResults = (List<TradeConvergenceAnalysisResult>) result;
        if (CollectionUtils.isEmpty(convergenceResults)) {
            return null;
        }
        return convergenceResults;
    }
}

