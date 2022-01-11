/**
 * @作者 Mcj
 */
package com.zqykj.app.service.interfaze.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zqykj.app.service.config.ThreadPoolConfig;
import com.zqykj.app.service.factory.parse.fund.FundTacticsAggResultParseFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.interfaze.IFundTacticsAnalysis;
import com.zqykj.app.service.factory.AggregationEntityMappingFactory;
import com.zqykj.app.service.factory.AggregationRequestParamFactory;
import com.zqykj.app.service.factory.QueryRequestParamFactory;
import com.zqykj.app.service.vo.fund.*;
import com.zqykj.common.core.ServerResponse;
import com.zqykj.domain.PageRequest;
import com.zqykj.domain.bank.BankTransactionFlow;
import com.zqykj.domain.bank.BankTransactionRecord;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.query.QuerySpecialParams;
import com.zqykj.repository.EntranceRepository;
import com.zqykj.util.JacksonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FundTacticsAnalysisImpl extends FundTacticsCommonImpl implements IFundTacticsAnalysis {

    private final EntranceRepository entranceRepository;

    private final AggregationRequestParamFactory aggregationRequestParamFactory;

    private final QueryRequestParamFactory queryRequestParamFactory;

    private final AggregationEntityMappingFactory aggregationEntityMappingFactory;

    private final FundTacticsAggResultParseFactory parseFactory;

    /**
     * <h2> 批量获取调单卡号集合 </h2>
     */
    public List<String> getAllMainCardsViaPageable(FundTacticsPartGeneralPreRequest request, int from, int size, String caseId) {

        // 构建查询参数
        QuerySpecialParams query = queryRequestParamFactory.buildBasicParamQueryViaCase(request, caseId);

        // 构建查询卡号去重聚合查询
        AggregationParams groupQueryCard = aggregationRequestParamFactory.buildGetCardNumsInBatchesAgg(from, size);
        // 聚合名称-属性映射(为了聚合对应聚合名称下的聚合值)
        Map<String, String> mapping = aggregationEntityMappingFactory.buildGroupByAggMapping(FundTacticsAnalysisField.QUERY_CARD);
        groupQueryCard.setMapping(mapping);
        // 定义该聚合的功能名称
        groupQueryCard.setResultName("groupQueryCard");
        // 一组调单卡号集合
        Map<String, List<List<Object>>> mainCardResults = entranceRepository.compoundQueryAndAgg(query, groupQueryCard, BankTransactionFlow.class, caseId);
        List<List<Object>> mainCards = mainCardResults.get("groupQueryCard");
        // 返回卡号
        return mainCards.stream().map(e -> e.get(0).toString()).collect(Collectors.toList());
    }

    public int getAllMainCardsCount(FundTacticsPartGeneralPreRequest request, String caseId) {

        // 构建查询参数
        QuerySpecialParams query = queryRequestParamFactory.buildBasicParamQueryViaCase(request, caseId);

        AggregationParams distinctQueryCard = aggregationRequestParamFactory.buildDistinctViaField(FundTacticsAnalysisField.QUERY_CARD);

        Map<String, String> mapping = aggregationEntityMappingFactory.buildDistinctTotalAggMapping(FundTacticsAnalysisField.QUERY_CARD);

        distinctQueryCard.setMapping(mapping);
        // 定义该聚合的功能名称
        distinctQueryCard.setResultName("distinctQueryCard");

        Map<String, List<List<Object>>> mainCardTotalResults = entranceRepository.compoundQueryAndAgg(query, distinctQueryCard, BankTransactionFlow.class, caseId);
        List<List<Object>> mainCardsTotal = mainCardTotalResults.get("distinctQueryCard");
        // 返回总量
        return Integer.parseInt(mainCardsTotal.get(0).get(0).toString());
    }

    /**
     * <h2> 异步过滤出调单卡号 </h2>
     */
    public Map<String, String> asyncFilterMainCards(String caseId, List<String> cards) throws ExecutionException, InterruptedException {

        int position = 0;
        int size = cards.size();
        Map<String, String> mainCards = new HashMap<>(size);
        List<CompletableFuture<Map<String, String>>> futures = new ArrayList<>();

        while (position < size) {
            int next = Math.min(position + chunkSize, size);
            int finalPosition = position;
            CompletableFuture<Map<String, String>> future = CompletableFuture.supplyAsync(() ->
                    filterMainCards(caseId, cards.subList(finalPosition, next)), ThreadPoolConfig.getExecutor());

            position = next;
            futures.add(future);
        }
        for (CompletableFuture<Map<String, String>> future : futures) {

            Map<String, String> mainCard = future.get();
            if (!CollectionUtils.isEmpty(mainCard)) {
                mainCards.putAll(mainCard);
            }
        }
        return mainCards;
    }

    /**
     * <h2> 从给定的卡号集合中过滤出 调单卡号集合 </h2>
     * 查询的表是 {@link BankTransactionFlow}
     */
    private Map<String, String> filterMainCards(String caseId, List<String> cards) {
        // 筛选出调单卡号集合的查询请求
        QuerySpecialParams query = queryRequestParamFactory.filterMainCards(caseId, cards);
        // 筛选出调单卡号的聚合请求
        AggregationParams agg = aggregationRequestParamFactory.groupByField(FundTacticsAnalysisField.QUERY_CARD, cards.size(), null);
        Map<String, String> mapping = aggregationEntityMappingFactory.buildGroupByAggMapping(FundTacticsAnalysisField.QUERY_CARD);
        agg.setMapping(mapping);
        agg.setResultName("groupByQueryCards");
        Map<String, List<List<Object>>> groupByMap = entranceRepository.compoundQueryAndAgg(query, agg, BankTransactionFlow.class, caseId);
        // 如果没有符合的调单卡号,直接退出返回
        if (CollectionUtils.isEmpty(groupByMap)) {
            return null;
        }
        List<List<Object>> results = groupByMap.get(agg.getResultName());
        if (CollectionUtils.isEmpty(results)) {
            return null;
        }
        return results.stream().collect(Collectors.toMap(e -> e.get(0).toString(), e -> e.get(0).toString(), (v1, v2) -> v1));
    }

    /**
     * <h2> 获取调单个体分析结果 </h2>
     */
    public ServerResponse getAdjustIndividuals(AdjustIndividualRequest request) {

        // 设置分组最大返回数量
        request.setGroupInitSize(initGroupSize);
        // 构建选择个体查询参数
        QuerySpecialParams query = queryRequestParamFactory.buildAdjustIndividualQuery(request);
        // 构建选择个体聚合参数
        AggregationParams agg = aggregationRequestParamFactory.buildAdjustIndividualAgg(request);
        // 构建选择个体总量参数
        AggregationParams totalAgg = aggregationRequestParamFactory.buildDistinctViaField(FundTacticsAnalysisField.CUSTOMER_IDENTITY_CARD);
        totalAgg.setMapping(aggregationEntityMappingFactory.buildDistinctTotalAggMapping(FundTacticsAnalysisField.CUSTOMER_IDENTITY_CARD));
        totalAgg.setResultName("cardinality_total");
        // 添加同级聚合
        agg.addSiblingAggregation(totalAgg);

        // 构建聚合名称属性映射(获取聚合值)
        Map<String, String> aggNameKeyMapping = new LinkedHashMap<>();
        // 构建聚合名称到实体属性之间的映射
        Map<String, String> aggNameEntityMapping = new LinkedHashMap<>();
        aggregationEntityMappingFactory.buildTradeAnalysisResultAggMapping(aggNameKeyMapping, aggNameEntityMapping, AdjustIndividualAnalysisResult.class);

        // 设置此聚合代表性功能名称
        agg.setResultName("selectIndividuals");

        // 获取聚合结果
        Map<String, List<List<Object>>> resultMap = entranceRepository.compoundQueryAndAgg(query, agg, BankTransactionRecord.class, request.getCaseId());
        List<List<Object>> results = resultMap.get(agg.getResultName());
        // 实体属性名称
        List<String> titles = new ArrayList<>(aggNameEntityMapping.keySet());
        // 实体属性与聚合值映射
        List<Map<String, Object>> entityMapping = parseFactory.convertEntity(results, titles, AdjustIndividualAnalysisResult.class);
        // 反序列化
        List<AdjustIndividualAnalysisResult> adjustIndividualResults = JacksonUtils.parse(JacksonUtils.toJson(entityMapping), new TypeReference<List<AdjustIndividualAnalysisResult>>() {
        });
        // 获取总量
        List<List<Object>> totalResults = resultMap.get(totalAgg.getResultName());
        long total;
        if (CollectionUtils.isEmpty(totalResults)) {
            return ServerResponse.createBySuccess(FundAnalysisResultResponse.empty());
        }
        total = (long) totalResults.get(0).get(0);
        FundAnalysisResultResponse<AdjustIndividualAnalysisResult> resultResponse = new FundAnalysisResultResponse<>();
        // 结果集
        resultResponse.setContent(adjustIndividualResults);
        // 每页显示条数
        resultResponse.setSize(request.getPageRequest().getPageSize());
        // 总数据量
        resultResponse.setTotal(total);
        // 总页数
        resultResponse.setTotalPages(PageRequest.getTotalPages(total, request.getPageRequest().getPageSize()));
        return ServerResponse.createBySuccess(resultResponse);
    }

    public ServerResponse getAdjustCardsViaIndividual(AdjustIndividualRequest request) {

        // 设置分组最大返回数量
        request.setGroupInitSize(initGroupSize);
        // 构建调单卡号查询
        QuerySpecialParams query = queryRequestParamFactory.buildAdjustIndividualQuery(request);
        // 构建调单卡号聚合
        AggregationParams agg = aggregationRequestParamFactory.buildAdjustCardsAgg(request);

        // 构建聚合名称属性映射(获取聚合值)
        Map<String, String> aggNameKeyMapping = new LinkedHashMap<>();
        // 构建聚合名称到实体属性之间的映射
        Map<String, String> aggNameEntityMapping = new LinkedHashMap<>();
        aggregationEntityMappingFactory.buildTradeAnalysisResultAggMapping(aggNameKeyMapping, aggNameEntityMapping, AdjustCardAnalysisResult.class);

        // 设置此聚合代表性功能名称
        agg.setResultName("adjustCards");

        // 获取聚合结果
        Map<String, List<List<Object>>> resultMap = entranceRepository.compoundQueryAndAgg(query, agg, BankTransactionRecord.class, request.getCaseId());
        List<List<Object>> results = resultMap.get(agg.getResultName());
        // 实体属性名称
        List<String> titles = new ArrayList<>(aggNameEntityMapping.keySet());
        // 实体属性与聚合值映射
        List<Map<String, Object>> entityMapping = parseFactory.convertEntity(results, titles, AdjustCardAnalysisResult.class);
        // 反序列化
        List<AdjustCardAnalysisResult> adjustCardsResults = JacksonUtils.parse(JacksonUtils.toJson(entityMapping), new TypeReference<List<AdjustCardAnalysisResult>>() {
        });
        return ServerResponse.createBySuccess(adjustCardsResults);
    }
}
