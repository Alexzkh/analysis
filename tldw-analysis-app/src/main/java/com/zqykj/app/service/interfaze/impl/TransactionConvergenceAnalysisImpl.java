/**
 * @作者 Mcj
 */
package com.zqykj.app.service.interfaze.impl;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.fasterxml.jackson.core.type.TypeReference;
import com.zqykj.app.service.config.ThreadPoolConfig;
import com.zqykj.app.service.factory.param.agg.TradeConvergenceAnalysisAggParamFactory;
import com.zqykj.app.service.factory.param.query.TradeConvergenceAnalysisQueryParamFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.field.FundTacticsFuzzyQueryField;
import com.zqykj.app.service.interfaze.IFundTacticsAnalysis;
import com.zqykj.app.service.interfaze.ITransactionConvergenceAnalysis;
import com.zqykj.app.service.vo.fund.*;
import com.zqykj.app.service.vo.fund.middle.TradeAnalysisDetailResult;
import com.zqykj.common.util.EasyExcelUtils;
import com.zqykj.domain.bank.BankTransactionRecord;
import com.zqykj.infrastructure.core.ServerResponse;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.query.QueryOperator;
import com.zqykj.parameters.query.QuerySpecialParams;
import com.zqykj.util.JacksonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class TransactionConvergenceAnalysisImpl extends FundTacticsCommonImpl implements ITransactionConvergenceAnalysis {

    private final TradeConvergenceAnalysisAggParamFactory tradeConvergenceAnalysisAggParamFactory;

    private final TradeConvergenceAnalysisQueryParamFactory tradeConvergenceAnalysisQueryParamFactory;

    private final IFundTacticsAnalysis fundTacticsAnalysis;

    @Override
    public ServerResponse<FundAnalysisResultResponse<TradeConvergenceAnalysisResult>> convergenceAnalysisResult(TradeConvergenceAnalysisQueryRequest request, int from, int size,
                                                                                                                boolean isComputeTotal) throws Exception {

        String caseId = request.getCaseId();
        Map<String, Object> map;
        if (request.getAnalysisType() == 2 || request.getAnalysisType() == 3) {
            // 设置分组桶的大小
            request.setGroupInitSize(fundThresholdConfig.getGroupByThreshold());
            map = convergenceAnalysisResultViaChosenMainCards(request, from, size, caseId, isComputeTotal);
        } else {
            // 全部查询
            map = convergenceAnalysisResultViaAllMainCards(request, from, size, caseId);
        }
        if (CollectionUtils.isEmpty(map)) {
            return ServerResponse.createBySuccess(FundAnalysisResultResponse.empty());
        }
        List<TradeConvergenceAnalysisResult> results = (List<TradeConvergenceAnalysisResult>) map.get("result");
        long total = Long.parseLong(String.valueOf(map.get("total")));
        return ServerResponse.createBySuccess(FundAnalysisResultResponse.build(results, total, size));
    }

    /**
     * <h2> 获取交易汇聚分析结果总量 </h2>
     */
    private long getConvergenceAnalysisResultTotal(TradeConvergenceAnalysisQueryRequest request) {
        // 构建 交易汇聚分析查询请求
        AggregationParams totalAgg = total(request);
        // 查询总量
        // 如果是全部查询,需要以全部调单卡号作为 查询卡号条件去查询
        if (CollectionUtils.isEmpty(request.getCardNum())) {
            QueryOperator operator = FundTacticsPartGeneralPreRequest.getOperator(request.getOperator());
            List<String> adjustCards = queryMaxAdjustCardsBySingleAmountDate(request.getCaseId(), request.getFund(), operator, FundTacticsPartGeneralRequest.getDateRange(request.getDateRange()));
            if (!CollectionUtils.isEmpty(adjustCards)) {
                request.setCardNum(adjustCards);
            }
        }
        QuerySpecialParams totalQuery = tradeConvergenceAnalysisQueryParamFactory.buildTradeConvergenceAnalysisResultMainCardsRequest(request, request.getCaseId());
        Map<String, List<List<Object>>> totalMap = entranceRepository.compoundQueryAndAgg(totalQuery, totalAgg, BankTransactionRecord.class, request.getCaseId());
        if (CollectionUtils.isEmpty(totalMap) || CollectionUtils.isEmpty(totalMap.get(CARDINALITY_TOTAL))) {
            return 0;
        }
        return (long) totalMap.get(CARDINALITY_TOTAL).get(0).get(0);
    }

    public ServerResponse<FundAnalysisResultResponse<TradeAnalysisDetailResult>> getDetail(FundTacticsPartGeneralRequest request) {

        com.zqykj.common.vo.PageRequest pageRequest = request.getPageRequest();
        return detail(request, pageRequest.getPage(), pageRequest.getPageSize(), FundTacticsFuzzyQueryField.detailFuzzyFields);
    }

    public ServerResponse<String> detailExport(ExcelWriter excelWriter, FundTacticsPartGeneralRequest request) throws Exception {

        int total = Integer.parseInt(String.valueOf(detailTotal(request)));
        // 详情数据不会有多少,直接一个sheet页解决
        writeSheet(excelWriter, total, request);
        return ServerResponse.createBySuccess();
    }

    public ServerResponse<String> convergenceAnalysisResultExport(ExcelWriter excelWriter, TradeConvergenceAnalysisQueryRequest request) throws Exception {

        int total = Integer.parseInt(String.valueOf(getConvergenceAnalysisResultTotal(request)));
        if (total == 0) {
            // 生成一个sheet
            WriteSheet sheet = EasyExcelUtils.generateWriteSheet(request.getExportFileName());
            excelWriter.write(new ArrayList<>(), sheet);
            return ServerResponse.createBySuccess();
        }
        int perRowSheetCount = exportThresholdConfig.getPerSheetRowCount();
        if (total <= perRowSheetCount) {
            // 单个sheet页即可
            WriteSheet sheet = EasyExcelUtils.generateWriteSheet(request.getExportFileName());
            writeSheetData(0, total, excelWriter, sheet, request);
        } else {
            // 多个sheet页处理
            int limit = total;
            if (total > exportThresholdConfig.getExcelExportThreshold()) {
                limit = exportThresholdConfig.getExcelExportThreshold();
            }
            int position = 0;
            int perSheetRowCount = exportThresholdConfig.getPerSheetRowCount();
            // 这里就没必要在多线程了(一个sheet页假设50W,内部分批次查询,每次查询1W,就要查詢50次,若这里再开多线程分批次,ThreadPoolConfig.getExecutor()
            // 的最大线程就这么多,剩下的只能在队列中等待)
            int sheetNo = 0;
            while (position < limit) {
                int next = Math.min(position + perSheetRowCount, limit);
                WriteSheet sheet;
                if (sheetNo == 0) {
                    sheet = EasyExcelUtils.generateWriteSheet(sheetNo, request.getExportFileName());
                } else {
                    sheet = EasyExcelUtils.generateWriteSheet(sheetNo, request.getExportFileName() + "_" + sheetNo);
                }
                writeSheetData(position, next, excelWriter, sheet, request);
                position = next;
                sheetNo++;
            }
        }
        return ServerResponse.createBySuccess();
    }

    /**
     * <h2> 批量将交易汇聚结果写入到Excel中 </h2>
     */
    private void writeSheetData(int position, int limit, ExcelWriter writer, WriteSheet sheet, TradeConvergenceAnalysisQueryRequest request) throws Exception {

        if (limit == 0) {
            return;
        }
        int chunkSize = exportThresholdConfig.getPerWriteRowCount();
        List<Future<List<TradeConvergenceAnalysisResult>>> futures = new ArrayList<>();
        while (position < limit) {
            int next = Math.min(position + chunkSize, limit);
            int finalPosition = position;
            Future<List<TradeConvergenceAnalysisResult>> future = CompletableFuture.supplyAsync(() -> {
                        try {
                            return convergenceAnalysisResult(request, finalPosition, next - finalPosition, false).getData().getContent();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    },
                    ThreadPoolConfig.getExecutor());
            position = next;
            futures.add(future);
        }
        for (Future<List<TradeConvergenceAnalysisResult>> future : futures) {
            List<TradeConvergenceAnalysisResult> dataList = future.get();
            // 添加sheet
            if (!CollectionUtils.isEmpty(dataList)) {
                writer.write(dataList, sheet);
            }
        }
    }

    /**
     * <h2> 选择个体 / 选择部分调单卡号集合 </h2>
     */
    @SuppressWarnings("all")
    protected Map<String, Object> convergenceAnalysisResultViaChosenMainCards(TradeConvergenceAnalysisQueryRequest request, int from, int size, String caseId, boolean isComputeTotal) {

        // 构建 交易汇聚分析查询请求
        QuerySpecialParams convergenceQuery = tradeConvergenceAnalysisQueryParamFactory.buildTradeConvergenceAnalysisResultMainCardsRequest(request, caseId);

        // 构建 交易汇聚分析聚合请求
        AggregationParams convergenceAgg = tradeConvergenceAnalysisAggParamFactory.buildTradeConvergenceAnalysisResultMainCardsAgg(request, from, size);

        // 构建 mapping (聚合名称 -> 聚合属性)  , (实体属性 -> 聚合名称)
        Map<String, String> aggMapping = new LinkedHashMap<>();
        Map<String, String> entityMapping = new LinkedHashMap<>();
        entityMappingFactory.buildTradeAnalysisResultAggMapping(aggMapping, entityMapping, TradeConvergenceAnalysisResult.class);
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
        List<Map<String, Object>> entityPropertyValueMapping = parseFactory.convertEntity(returnResults, entityTitles, TradeConvergenceAnalysisResult.class);
        // 反序列化实体
        List<TradeConvergenceAnalysisResult> tradeConvergenceAnalysisResults = JacksonUtils.parse(entityPropertyValueMapping, new TypeReference<List<TradeConvergenceAnalysisResult>>() {
        });
        // 将金额保留2位小数,转化科学计算方式的金额
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
                map.put("total", Integer.parseInt(String.valueOf(total.get(0).get(0))));
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
        QuerySpecialParams query = tradeConvergenceAnalysisQueryParamFactory.buildTradeConvergenceAnalysisResultMainCardsRequest(request, caseId);
        // 构建 交易汇聚分析聚合请求
        AggregationParams agg = tradeConvergenceAnalysisAggParamFactory.buildTradeConvergenceQueryAndMergeCardsAgg(request, from, size);
        // 构建 mapping (聚合名称 -> 聚合属性)
        Map<String, String> mapping = entityMappingFactory.buildGroupByAggMapping(FundTacticsAnalysisField.MERGE_CARD);
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
        AggregationParams totalAgg = tradeConvergenceAnalysisAggParamFactory.buildTradeConvergenceAnalysisResultTotalAgg(request);
        totalAgg.setMapping(entityMappingFactory.buildDistinctTotalAggMapping(FundTacticsAnalysisField.MERGE_CARD));
        totalAgg.setResultName(CARDINALITY_TOTAL);
        return totalAgg;
    }

    /**
     * <h2> 按照本方查询卡号(即是全部调单卡号进行分析) </h2>
     * <p>
     * 分析的结果: 其中交易卡号出现的必须是调单的(无论它原来是在本方还是对方)
     */
    @SuppressWarnings("all")
    protected Map<String, Object> convergenceAnalysisResultViaAllMainCards(TradeConvergenceAnalysisQueryRequest request, int from, int limit, String caseId) throws Exception {

        Map<String, Object> resultMap = new HashMap<>();
        // 检查调单卡号数量是否超过了限制,没有的话查询最大调单卡号数量作为条件
        QueryOperator operator = FundTacticsPartGeneralPreRequest.getOperator(request.getOperator());
        if (checkAdjustCardCountBySingleAmountDate(request.getCaseId(), request.getFund(), operator, FundTacticsPartGeneralRequest.getDateRange(request.getDateRange()))) {
            List<String> adjustCards = queryMaxAdjustCardsBySingleAmountDate(request.getCaseId(), request.getFund(), operator, FundTacticsPartGeneralRequest.getDateRange(request.getDateRange()));
            if (CollectionUtils.isEmpty(adjustCards)) {
                resultMap.put("total", 0);
                resultMap.put("result", new ArrayList<>());
                return resultMap;
            }
            request.setCardNum(adjustCards);
            return convergenceAnalysisResultViaChosenMainCards(request, from, limit, request.getCaseId(), true);
        }
        // 异步执行 全部查询任务
        // 获取全部查询的总量
        AggregationParams totalAgg = total(request);
        // 构建 交易汇聚分析查询请求
        QuerySpecialParams convergenceQuery = tradeConvergenceAnalysisQueryParamFactory.buildTradeConvergenceAnalysisResultMainCardsRequest(request, caseId);
        Map<String, List<List<Object>>> totalResults = entranceRepository.compoundQueryAndAgg(convergenceQuery, totalAgg, BankTransactionRecord.class, caseId);
        // 这里计算的总量其实不是正确的、但是后面拿出的分页数据是正确的
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
        // 设置分组数量
        request.setGroupInitSize(fundThresholdConfig.getGroupByThreshold());
        // 异步任务查询起始位置
        int position = 0;
        // 异步任务查询总量
        int size = Integer.parseInt(String.valueOf(total));
        // 异步任务查询批处理阈值
        int chunkSize = fundThresholdConfig.getPerTotalSplitCount();
        ThreadPoolTaskExecutor executor = ThreadPoolConfig.getExecutor();
        // 需要返回的数量
        List<String> mergeCards = new ArrayList<>();
        StopWatch stopWatch = StopWatch.createStarted();
        while (position < size) {
            int next = Math.min(position + chunkSize, size);
            Future<List<String>> future = executor.submit(new ConvergenceFutureTask(position,
                    next, from, limit, caseId, request));
            List<String> results = future.get();
            if (!CollectionUtils.isEmpty(results)) {
                mergeCards.addAll(results);
            }
            if (mergeCards.size() == limit) {
                break;
            } else {
                if (mergeCards.size() > 0) {
                    from = 0;
                    limit = limit - mergeCards.size();
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
        int chunkSize = fundThresholdConfig.getPerTotalSplitQueryCount();
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

        if (CollectionUtils.isEmpty(mergeCards)) {
            return new ArrayList<>();
        }
        // 构建查询参数
        QuerySpecialParams condition = tradeConvergenceAnalysisQueryParamFactory.buildTradeConvergenceAnalysisHitsQuery(mergeCards, caseId);
        // 构建聚合参数
        AggregationParams agg = tradeConvergenceAnalysisAggParamFactory.buildTradeConvergenceAnalysisHitsAgg(mergeCards.size());
        Map<String, String> map = entityMappingFactory.buildShowFieldsAggMapping();
        agg.setMapping(map);
        agg.setResultName("hits");
        Map<String, List<List<Object>>> resultMap = entranceRepository.compoundQueryAndAgg(condition, agg, BankTransactionRecord.class, caseId);
        List<Map<String, Object>> results = parseFactory.convertEntity(resultMap.get(agg.getResultName()), new ArrayList<>(), TradeConvergenceAnalysisResult.class);
        return JacksonUtils.parse(results, new TypeReference<List<TradeConvergenceAnalysisResult>>() {
        });
    }

    /**
     * <h2> 补充交易汇聚分析结果聚合展示字段 </h2>
     */
    private void addTradeConvergenceAnalysisShowFields(List<TradeConvergenceAnalysisResult> convergenceAnalysisResults,
                                                       List<TradeConvergenceAnalysisResult> hits) {
        // 把一方生成map(避免双层for循环,时间复杂度降低)
        Map<String, TradeConvergenceAnalysisResult> hitsMap = hits.stream().collect(Collectors.toMap(TradeConvergenceAnalysisResult::getMergeCard, e -> e, (v1, v2) -> v1));
        for (TradeConvergenceAnalysisResult convergenceAnalysisResult : convergenceAnalysisResults) {

            TradeConvergenceAnalysisResult hit = hitsMap.get(convergenceAnalysisResult.getMergeCardKey());
            if (null != hit) {
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

