/**
 * @作者 Mcj
 */
package com.zqykj.app.service.interfaze.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zqykj.app.service.config.ThreadPoolConfig;
import com.zqykj.app.service.factory.param.agg.TransactionFieldAggParamFactory;
import com.zqykj.app.service.factory.param.query.TransactionFieldQueryParamFactory;
import com.zqykj.app.service.interfaze.ITransactionField;
import com.zqykj.app.service.vo.fund.*;
import com.zqykj.builder.AggregationParamsBuilders;
import com.zqykj.common.core.ServerResponse;
import com.zqykj.common.vo.PageRequest;
import com.zqykj.domain.bank.BankTransactionFlow;
import com.zqykj.parameters.Pagination;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.query.QuerySpecialParams;
import com.zqykj.util.JacksonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * <h1> 交易字段分析 </h1>
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class TransactionFieldImpl extends FundTacticsCommonImpl implements ITransactionField {

    private final TransactionFieldQueryParamFactory transactionFieldQueryParamFactory;

    private final TransactionFieldAggParamFactory transactionFieldAggParamFactory;

    public ServerResponse<List<TransactionFieldTypeProportionResults>> fieldTypeProportionHistogram(TransactionFieldAnalysisRequest request) throws ExecutionException, InterruptedException {

        Future<List<TransactionFieldTypeProportionResults>> future = CompletableFuture.supplyAsync(() ->
                tradeFieldTypeQuery(request, TransactionFieldTypeProportionResults.class), ThreadPoolConfig.getExecutor());
        List<TransactionFieldTypeCustomResults> customResults = batchCustomCollationQuery(request);
        List<TransactionFieldTypeProportionResults> proportionResults = future.get();
        if (!CollectionUtils.isEmpty(customResults)) {
            // 转成交易类型占比结果
            proportionResults.addAll(TransactionFieldTypeProportionResults.convertProportionResults(customResults));
        }
        // 合并排序(将汇总后的占比结果重新排序)
        List<TransactionFieldTypeProportionResults> mergeSortResult = TransactionFieldTypeProportionResults.mergeSort(request, proportionResults);
        return ServerResponse.createBySuccess(mergeSortResult);
    }

    public ServerResponse<FundAnalysisResultResponse<TransactionFieldTypeStatisticsResult>> fieldTypeStatistics(int from, int size, TransactionFieldAnalysisRequest request) throws ExecutionException, InterruptedException {

        request.setAggQueryType(2);
        Future<List<TransactionFieldTypeStatisticsResult>> future = CompletableFuture.supplyAsync(() ->
                tradeFieldTypeQuery(request, TransactionFieldTypeStatisticsResult.class), ThreadPoolConfig.getExecutor());
        List<TransactionFieldTypeCustomResults> customResults = batchCustomCollationQuery(request);
        List<TransactionFieldTypeStatisticsResult> statisticsResults = future.get();
        if (!CollectionUtils.isEmpty(customResults)) {
            // 转成交易类型统计结果
            statisticsResults.addAll(TransactionFieldTypeStatisticsResult.convertStatisticsResults(customResults));
        }
        // 合并排序(将汇总后的交易统计结果重新排序)
        List<TransactionFieldTypeStatisticsResult> mergeSortResult = TransactionFieldTypeStatisticsResult.mergeSort(request, statisticsResults);
        long total = total(request);
        return ServerResponse.createBySuccess(FundAnalysisResultResponse.build(mergeSortResult, total, size));
    }

    public ServerResponse<FundAnalysisResultResponse<Object>> customCollationContainField(TransactionFieldAnalysisRequest request) {

        PageRequest pageRequest = request.getPageRequest();
        int offset = PageRequest.getOffset(pageRequest.getPage(), pageRequest.getPageSize());
        QuerySpecialParams query = transactionFieldQueryParamFactory.transactionFieldTypeQuery(request);
        AggregationParams agg = aggParamFactory.groupByField(request.getStatisticsField(), fundThresholdConfig.getGroupByThreshold(),
                new Pagination(offset, pageRequest.getPageSize()));
        agg.setMapping(entityMappingFactory.buildGroupByAggMapping(request.getStatisticsField()));
        agg.setResultName("groupByStatisticsField");
        agg.addSiblingAggregation(totalAgg(request.getStatisticsField()));
        Map<String, List<List<Object>>> resultMap = entranceRepository.compoundQueryAndAgg(query, agg, BankTransactionFlow.class, request.getCaseId());
        List<List<Object>> resultList = resultMap.get(agg.getResultName());
        List<Object> result = resultList.stream().map(e -> e.get(0)).collect(Collectors.toList());
        List<List<Object>> totalResult = resultMap.get(CARDINALITY_TOTAL);
        long total;
        if (CollectionUtils.isEmpty(totalResult)) {
            total = 0L;
        } else {
            total = (long) totalResult.get(0).get(0);
        }
        return ServerResponse.createBySuccess(FundAnalysisResultResponse.build(result, total, pageRequest.getPageSize()));
    }

    /**
     * <h2> 交易字段类型(自定义归类查询) - 包含字段类型占比/字段类型统计 </h2>
     */
    public List<TransactionFieldTypeCustomResults> batchCustomCollationQuery(TransactionFieldAnalysisRequest request) throws ExecutionException, InterruptedException {

        List<TransactionFieldAnalysisRequest.CustomCollationQueryRequest> customCollationQueryRequests = request.getCustomCollationQueryRequests();
        if (CollectionUtils.isEmpty(customCollationQueryRequests)) {
            return null;
        }
        List<Future<List<TransactionFieldTypeCustomResults>>> futures = new ArrayList<>();
        for (TransactionFieldAnalysisRequest.CustomCollationQueryRequest customCollationQueryRequest : customCollationQueryRequests) {

            Future<List<TransactionFieldTypeCustomResults>> future = CompletableFuture.supplyAsync(
                    () -> tradeFieldTypeCustomCollationQuery(request, customCollationQueryRequest.getContainField(), TransactionFieldTypeCustomResults.class), ThreadPoolConfig.getExecutor());
            futures.add(future);
        }
        List<TransactionFieldTypeCustomResults> results = new ArrayList<>();
        int i = 0;
        for (Future<List<TransactionFieldTypeCustomResults>> future : futures) {
            List<TransactionFieldTypeCustomResults> transactionFieldTypeProportionResults = future.get();
            if (!CollectionUtils.isEmpty(transactionFieldTypeProportionResults)) {
                TransactionFieldTypeCustomResults proportionResult = transactionFieldTypeProportionResults.get(i);
                proportionResult.setFieldGroupContent(customCollationQueryRequests.get(i).getClassificationName());
                results.add(proportionResult);
            }
            i++;
        }
        return results;
    }

    /**
     * <h2> 交易字段类型查询- 包含字段类型占比/字段类型统计</h2>
     */
    public <T> List<T> tradeFieldTypeQuery(TransactionFieldAnalysisRequest request, Class<T> entity) {
        return tradeFieldTypeCustomCollationQuery(request, null, entity);
    }

    /**
     * <h2> 交易字段类型结果查询(属于自定义归类查询)- 包含字段类型占比/字段类型统计 </h2>
     */
    public <T> List<T> tradeFieldTypeCustomCollationQuery(TransactionFieldAnalysisRequest request, List<String> containFieldContent, Class<T> entity) {
        QuerySpecialParams query;
        AggregationParams agg;
        PageRequest pageRequest = request.getPageRequest();
        Map<String, String> keyMapping = new LinkedHashMap<>();
        Map<String, String> entityMapping = new LinkedHashMap<>();
        if (!CollectionUtils.isEmpty(containFieldContent)) {
            query = transactionFieldQueryParamFactory.fieldTypeCustomCollationQuery(request, containFieldContent);
            agg = transactionFieldAggParamFactory.fieldTypeProportionCustomCollationQuery(request, fundThresholdConfig.getGroupByThreshold());
            entityMappingFactory.buildTradeAnalysisResultAggMapping(keyMapping, entityMapping, entity, Arrays.asList("tradeTotalAmount", "tradeTotalTimes"));
        } else {
            query = transactionFieldQueryParamFactory.transactionFieldTypeQuery(request);
            agg = transactionFieldAggParamFactory.transactionFieldTypeProportion(request, 0, pageRequest.getPageSize(), fundThresholdConfig.getGroupByThreshold());
            entityMappingFactory.buildTradeAnalysisResultAggMapping(keyMapping, entityMapping, entity);
        }
        agg.setMapping(keyMapping);
        if (!CollectionUtils.isEmpty(containFieldContent)) {
            keyMapping.remove("field_group");
            entityMapping.remove("fieldGroupContent");
        }
        agg.setResultName("transactionFieldTypeResult");
        Map<String, List<List<Object>>> resultMap = entranceRepository.compoundQueryAndAgg(query, agg, BankTransactionFlow.class, request.getCaseId());
        List<List<Object>> resultList = resultMap.get(agg.getResultName());
        if (CollectionUtils.isEmpty(resultList)) {
            return new ArrayList<>();
        }
        List<String> entityTitles = new ArrayList<>(entityMapping.keySet());
        List<Map<String, Object>> maps = parseFactory.convertEntity(resultList, entityTitles, entity);
        return JacksonUtils.parse(maps, new TypeReference<List<T>>() {
        });
    }

    protected AggregationParams totalAgg(String statisticsField) {
        AggregationParams totalAgg = AggregationParamsBuilders.cardinality("distinct_" + statisticsField, statisticsField);
        totalAgg.setResultName(CARDINALITY_TOTAL);
        totalAgg.setMapping(entityMappingFactory.buildDistinctTotalAggMapping(statisticsField));
        return totalAgg;
    }

    /**
     * <h2> 交易字段类型 </h2>
     */
    private long total(TransactionFieldAnalysisRequest request) {

        QuerySpecialParams query = transactionFieldQueryParamFactory.transactionFieldTypeQuery(request);
        AggregationParams agg = totalAgg(request.getStatisticsField());
        Map<String, List<List<Object>>> resultMap = entranceRepository.compoundQueryAndAgg(query, agg, BankTransactionFlow.class, request.getCaseId());
        List<List<Object>> resultList = resultMap.get(agg.getResultName());
        if (CollectionUtils.isEmpty(resultList)) {
            return 0L;
        }
        return (long) resultList.get(0).get(0);
    }
}
