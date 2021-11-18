/**
 * @作者 Mcj
 */
package com.zqykj.app.service.interfaze.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zqykj.app.service.config.ThreadPoolConfig;
import com.zqykj.app.service.interfaze.ITransactionConvergenceAnalysis;
import com.zqykj.app.service.factory.AggregationEntityMappingFactory;
import com.zqykj.app.service.factory.AggregationRequestParamFactory;
import com.zqykj.app.service.factory.AggregationResultEntityParseFactory;
import com.zqykj.app.service.factory.QueryRequestParamFactory;
import com.zqykj.app.service.vo.fund.TradeConvergenceAnalysisResultResponse;
import com.zqykj.app.service.vo.fund.TradeConvergenceAnalysisQueryRequest;
import com.zqykj.app.service.vo.fund.TradeConvergenceAnalysisResult;
import com.zqykj.app.service.vo.fund.TradeStatisticalAnalysisResult;
import com.zqykj.domain.Page;
import com.zqykj.domain.PageRequest;
import com.zqykj.domain.bank.BankTransactionFlow;
import com.zqykj.domain.bank.BankTransactionRecord;
import com.zqykj.infrastructure.core.ServerResponse;
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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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

    @Value("${buckets.page.initSize}")
    private int initGroupSize;

    private static final String CARDINALITY_TOTAL = "cardinality_total";

    @Override
    public ServerResponse<TradeConvergenceAnalysisResultResponse> convergenceAnalysisResult(TradeConvergenceAnalysisQueryRequest request, String caseId) throws ExecutionException, InterruptedException {

        TradeConvergenceAnalysisResultResponse resultResponse = new TradeConvergenceAnalysisResultResponse();

        List<TradeConvergenceAnalysisResult> results;
        long total;
        if (request.getSearchType() == 0 && !CollectionUtils.isEmpty(request.getCardNums())) {

            Map<String, Object> map = convergenceAnalysisResultViaChosenMainCards(request, caseId, true);
            results = (List<TradeConvergenceAnalysisResult>) map.get("result");
            total = (long) map.get("total");
        } else {

            // 全部条件
            Map<String, Object> map = convergenceAnalysisResultViaAllMainCards(request, caseId);
            results = (List<TradeConvergenceAnalysisResult>) map.get("result");
            total = (long) map.get("total");
        }
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
     * <h2> 按照本方查询卡号(即是全部调单卡号进行分析) </h2>
     * <p>
     * 分析的结果: 其中交易卡号出现的必须是调单的(无论它原来是在本方还是对方)
     */
    protected Map<String, Object> convergenceAnalysisResultViaAllMainCards(TradeConvergenceAnalysisQueryRequest request, String caseId) throws ExecutionException, InterruptedException {

        // 异步执行 全部查询任务
        // 获取全部查询的总量
        AggregationParams totalAgg = total(request);
        // 构建 交易汇聚分析查询请求
        QuerySpecialParams convergenceQuery = queryRequestParamFactory.buildTradeConvergenceAnalysisResultMainCardsRequest(request, caseId);
        Map<String, List<List<Object>>> totalResults = entranceRepository.compoundQueryAndAgg(convergenceQuery, totalAgg, BankTransactionRecord.class, caseId);
        List<List<Object>> list = totalResults.get(CARDINALITY_TOTAL);
        long total = 0;
        if (!CollectionUtils.isEmpty(list)) {
            total = (long) list.get(0).get(0);
        }
        // 因为es 计算的去重总量是一个近似值,因此可能总量会少(这里需要调整一下)
        long computeTotal = total + total / 10;
        com.zqykj.common.vo.PageRequest pageRequest = request.getPageRequest();
        int position = com.zqykj.common.vo.PageRequest.getOffset(pageRequest.getPage(), pageRequest.getPageSize());
        int size = Integer.parseInt(String.valueOf(computeTotal));
        int chunkSize = 3000;
        ThreadPoolTaskExecutor executor = ThreadPoolConfig.getExecutor();
        int pageSize = pageRequest.getPageSize();
        List<Future<List<TradeConvergenceAnalysisResult>>> futures = new ArrayList<>();
        StopWatch stopWatch = StopWatch.createStarted();
        while (position < size) {
            int next = Math.min(position + chunkSize, size);
            Future<List<TradeConvergenceAnalysisResult>> future = executor.submit(new ConvergenceFutureTask(position, next, pageSize, caseId, request));
            position = next;
            futures.add(future);
        }
        List<TradeConvergenceAnalysisResult> convergenceAnalysisResults = new ArrayList<>();
        for (Future<List<TradeConvergenceAnalysisResult>> future : futures) {
            if (convergenceAnalysisResults.size() == pageSize) {
                // 取消后续执行的异步任务
                future.cancel(true);
            }
            List<TradeConvergenceAnalysisResult> results = future.get();
            if (!CollectionUtils.isEmpty(results)) {
                convergenceAnalysisResults.addAll(results);
            }
        }
        stopWatch.stop();
        log.info("async compute convergence analysis results cost time = {} ms", stopWatch.getTime(TimeUnit.MILLISECONDS));
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("total", total);
        resultMap.put("result", convergenceAnalysisResults);

        return resultMap;
    }

    /**
     * <h2> 选择个体 / 选择部分调单卡号集合 </h2>
     */
    @SuppressWarnings("all")
    protected Map<String, Object> convergenceAnalysisResultViaChosenMainCards(TradeConvergenceAnalysisQueryRequest request, String caseId, boolean isComputeTotal) {

        // 设置分组桶的大小
        request.setGroupInitSize(initGroupSize);

        // 构建 交易汇聚分析查询请求
        QuerySpecialParams convergenceQuery = queryRequestParamFactory.buildTradeConvergenceAnalysisResultMainCardsRequest(request, caseId);

        // 构建 交易汇聚分析聚合请求
        AggregationParams convergenceAgg = aggregationRequestParamFactory.buildTradeConvergenceAnalysisResultMainCardsAgg(request);

        // 构建 mapping (聚合名称 -> 聚合属性)  , (实体属性 -> 聚合名称)
        Map<String, String> aggMapping = new LinkedHashMap<>();
        Map<String, String> entityMapping = new LinkedHashMap<>();
        aggregationEntityMappingFactory.buildTradeAnalysisResultAggMapping(aggMapping, entityMapping, TradeConvergenceAnalysisResult.class);
        convergenceAgg.setMapping(aggMapping);
        convergenceAgg.setEntityAggColMapping(entityMapping);
        convergenceAgg.setResultName("chosen_main_cards");

        // 设置同级聚合(计算总数据量) 注: es 计算的去重后数量 是一个近似值
        // 判断是否需要计算总量
        if (isComputeTotal) {
            AggregationParams totalAgg = total(request);
            if (null != totalAgg) {
                convergenceAgg.addSiblingAggregation(totalAgg);
            }
        }
        Map<String, List<List<Object>>> results = entranceRepository.compoundQueryAndAgg(convergenceQuery, convergenceAgg, BankTransactionRecord.class, caseId);
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

        Map<String, Object> map = new HashMap<>();

        List<List<Object>> total = results.get(CARDINALITY_TOTAL);

        if (CollectionUtils.isEmpty(total)) {
            map.put("total", 0);
        } else {
            map.put("total", total.get(0).get(0));
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
     * <h2> 汇聚分析异步任务查询类(针对全部查询) </h2>
     */
    class ConvergenceFutureTask implements Callable<List<TradeConvergenceAnalysisResult>> {

        private int position;

        private int next;

        private String caseId;

        private int originSize;

        private TradeConvergenceAnalysisQueryRequest request;

        public ConvergenceFutureTask(int position, int next, int size,
                                     String caseId, TradeConvergenceAnalysisQueryRequest request) {
            this.position = position;
            this.next = next;
            this.originSize = size;
            this.caseId = caseId;
            this.request = request;
        }

        @Override
        public List<TradeConvergenceAnalysisResult> call() throws Exception {

            StopWatch stopWatch = StopWatch.createStarted();
            request.getPageRequest().setPage(position);
            request.getPageRequest().setPageSize(next);
            Map<String, Object> map = convergenceAnalysisResultViaChosenMainCards(request, caseId, false);
            Object result = map.get("result");
            List<TradeConvergenceAnalysisResult> convergenceResults = (List<TradeConvergenceAnalysisResult>) result;
            if (CollectionUtils.isEmpty(convergenceResults)) {
                return null;
            }
            // 需要从convergenceResults筛选出 调单的情况
            List<String> cards = convergenceResults.stream().map(TradeStatisticalAnalysisResult::getTradeCard).collect(Collectors.toList());
            int size = cards.size();
            // 筛选出调单卡号集合的查询请求
            QuerySpecialParams query = queryRequestParamFactory.filterMainCards(request, caseId, cards);
            Page<BankTransactionFlow> page = entranceRepository.findAll(PageRequest.of(0, size), caseId, BankTransactionFlow.class, query);
            List<BankTransactionFlow> content = page.getContent();
            // 如果没有符合的调单卡号,直接退出返回
            if (CollectionUtils.isEmpty(content)) {
                return null;
            }
            // 过滤出的调单卡号集合
            Map<String, String> filterMainCards = content.stream().collect(Collectors.toMap(BankTransactionFlow::getQueryCard, BankTransactionFlow::getQueryCard));
            // 返回最终的调单数据
            List<TradeConvergenceAnalysisResult> finalCards = convergenceResults.stream().filter(e -> filterMainCards.containsKey(e.getTradeCard()))
                    .limit(originSize).collect(Collectors.toList());
            log.info("Current Thread  = {} ,filter mainCards  cost time = {} ms", Thread.currentThread().getName(), stopWatch.getTime(TimeUnit.MILLISECONDS));
            return finalCards;
        }
    }
}

