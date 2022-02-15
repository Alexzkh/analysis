/**
 * @作者 Mcj
 */
package com.zqykj.app.service.interfaze.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zqykj.app.service.config.ThreadPoolConfig;
import com.zqykj.app.service.factory.param.agg.TransactionFieldAggParamFactory;
import com.zqykj.app.service.factory.param.query.TransactionFieldQueryParamFactory;
import com.zqykj.app.service.interfaze.ITransactionField;
import com.zqykj.app.service.vo.fund.FundAnalysisResultResponse;
import com.zqykj.app.service.vo.fund.TransactionFieldAnalysisRequest;
import com.zqykj.app.service.vo.fund.TransactionFieldTypeProportionResults;
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    private final TransactionFieldQueryParamFactory queryParamFactory;

    private final TransactionFieldAggParamFactory transactionFieldAggParamFactory;

    public ServerResponse<List<TransactionFieldTypeProportionResults>> fieldTypeProportionHistogram(TransactionFieldAnalysisRequest request) throws ExecutionException, InterruptedException {

        Future<List<TransactionFieldTypeProportionResults>> future = CompletableFuture.supplyAsync(() ->
                tradeFieldTypeProportionQuery(request), ThreadPoolConfig.getExecutor());
        List<TransactionFieldTypeProportionResults> results = batchCustomCollationQuery(request);
        List<TransactionFieldTypeProportionResults> customCollationResult = future.get();
        results.addAll(customCollationResult);
        // 合并排序(将自定义归类查询结果 与 主要查询结果合并,重新排序)
        List<TransactionFieldTypeProportionResults> mergeSortResult = TransactionFieldTypeProportionResults.mergeSort(request, results);
        return ServerResponse.createBySuccess(mergeSortResult);
    }

    public ServerResponse<FundAnalysisResultResponse<TransactionFieldTypeProportionResults>> fieldTypeStatistics(int from, int size, TransactionFieldAnalysisRequest request) {

        return null;
    }

    public ServerResponse<FundAnalysisResultResponse<Object>> customCollationContainField(TransactionFieldAnalysisRequest request) {

        PageRequest pageRequest = request.getPageRequest();
        int offset = PageRequest.getOffset(pageRequest.getPage(), pageRequest.getPageSize());
        QuerySpecialParams query = queryParamFactory.transactionFieldType(request);
        AggregationParams agg = aggParamFactory.groupByField(request.getStatisticsField(), fundThresholdConfig.getGroupByThreshold(),
                new Pagination(offset, pageRequest.getPageSize()));
        agg.setMapping(entityMappingFactory.buildGroupByAggMapping(request.getStatisticsField()));
        agg.setResultName("groupByStatisticsField");
        agg.addSiblingAggregation(total(request.getStatisticsField()));
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
     * <h2> 交易字段类型占比-自定义归类查询 </h2>
     */
    public List<TransactionFieldTypeProportionResults> batchCustomCollationQuery(TransactionFieldAnalysisRequest request) throws ExecutionException, InterruptedException {

        List<TransactionFieldAnalysisRequest.CustomCollationQueryRequest> customCollationQueryRequests = request.getCustomCollationQueryRequests();
        if (CollectionUtils.isEmpty(customCollationQueryRequests)) {
            return null;
        }
        List<Future<List<TransactionFieldTypeProportionResults>>> futures = new ArrayList<>();
        for (TransactionFieldAnalysisRequest.CustomCollationQueryRequest customCollationQueryRequest : customCollationQueryRequests) {

            Future<List<TransactionFieldTypeProportionResults>> future = CompletableFuture.supplyAsync(
                    () -> tradeFieldTypeProportionCustomCollationQuery(request, customCollationQueryRequest.getContainField()), ThreadPoolConfig.getExecutor());
            futures.add(future);
        }
        List<TransactionFieldTypeProportionResults> results = new ArrayList<>();
        for (Future<List<TransactionFieldTypeProportionResults>> future : futures) {
            List<TransactionFieldTypeProportionResults> transactionFieldTypeProportionResults = future.get();
            if (!CollectionUtils.isEmpty(transactionFieldTypeProportionResults)) {
                results.addAll(transactionFieldTypeProportionResults);
            }
        }
        return results;
    }

    /**
     * <h2> 交易字段类型占比查询 </h2>
     */
    public List<TransactionFieldTypeProportionResults> tradeFieldTypeProportionQuery(TransactionFieldAnalysisRequest request) {
        return tradeFieldTypeProportionCustomCollationQuery(request, null);
    }

    /**
     * <h2> 交易字段类型占比结果查询(属于自定义归类查询) </h2>
     */
    public List<TransactionFieldTypeProportionResults> tradeFieldTypeProportionCustomCollationQuery(TransactionFieldAnalysisRequest request, List<String> containFieldContent) {
        QuerySpecialParams query;
        if (!CollectionUtils.isEmpty(containFieldContent)) {
            query = queryParamFactory.transactionFieldCustomCollationQuery(request, containFieldContent);
        } else {
            query = queryParamFactory.transactionFieldType(request);
        }
        PageRequest pageRequest = request.getPageRequest();
        AggregationParams agg = transactionFieldAggParamFactory.transactionFieldTypeProportion(request, 0, pageRequest.getPageSize(), fundThresholdConfig.getGroupByThreshold());
        Map<String, String> keyMapping = new LinkedHashMap<>();
        Map<String, String> entityMapping = new LinkedHashMap<>();
        entityMappingFactory.buildTradeAnalysisResultAggMapping(keyMapping, entityMapping, TransactionFieldTypeProportionResults.class);
        agg.setResultName("transactionFieldProportionResult");
        Map<String, List<List<Object>>> resultMap = entranceRepository.compoundQueryAndAgg(query, agg, BankTransactionFlow.class, request.getCaseId());
        List<List<Object>> resultList = resultMap.get(agg.getResultName());
        if (CollectionUtils.isEmpty(resultList)) {
            return new ArrayList<>();
        }
        List<String> entityTitles = new ArrayList<>(entityMapping.keySet());
        List<Map<String, Object>> maps = parseFactory.convertEntity(resultList, entityTitles, TransactionFieldAnalysisRequest.class);
        return JacksonUtils.parse(maps, new TypeReference<List<TransactionFieldTypeProportionResults>>() {
        });
    }


    public AggregationParams total(String statisticsField) {

        AggregationParams totalAgg = AggregationParamsBuilders.cardinality("total", statisticsField);
        totalAgg.setResultName(CARDINALITY_TOTAL);
        totalAgg.setMapping(entityMappingFactory.buildDistinctTotalAggMapping(statisticsField));
        return totalAgg;
    }
}
