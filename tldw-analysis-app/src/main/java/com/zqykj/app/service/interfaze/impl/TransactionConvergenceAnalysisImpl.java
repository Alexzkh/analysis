/**
 * @作者 Mcj
 */
package com.zqykj.app.service.interfaze.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zqykj.app.service.interfaze.IFundTacticsAnalysis;
import com.zqykj.app.service.interfaze.ITransactionConvergenceAnalysis;
import com.zqykj.app.service.interfaze.factory.AggregationEntityMappingFactory;
import com.zqykj.app.service.interfaze.factory.AggregationRequestParamFactory;
import com.zqykj.app.service.interfaze.factory.AggregationResultEntityParseFactory;
import com.zqykj.app.service.interfaze.factory.QueryRequestParamFactory;
import com.zqykj.app.service.vo.fund.TradeConvergenceAnalysisResultResponse;
import com.zqykj.app.service.vo.fund.TradeConvergenceAnalysisQueryRequest;
import com.zqykj.app.service.vo.fund.TradeConvergenceAnalysisResult;
import com.zqykj.domain.PageRequest;
import com.zqykj.domain.bank.BankTransactionRecord;
import com.zqykj.infrastructure.core.ServerResponse;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TransactionConvergenceAnalysisImpl implements ITransactionConvergenceAnalysis {

    private final EntranceRepository entranceRepository;

    private final AggregationRequestParamFactory aggregationRequestParamFactory;

    private final QueryRequestParamFactory queryRequestParamFactory;

    private final AggregationEntityMappingFactory aggregationEntityMappingFactory;

    private final AggregationResultEntityParseFactory aggregationResultEntityParseFactory;

    private final IFundTacticsAnalysis fundTacticsAnalysis;

    @Value("${buckets.page.initSize}")
    private int initGroupSize;

    private static final String CARDINALITY_TOTAL = "cardinality_total";

    private final int MAIN_CARD_SIZE = 5000;

    @Override
    public ServerResponse<TradeConvergenceAnalysisResultResponse> convergenceAnalysisResult(TradeConvergenceAnalysisQueryRequest request, String caseId) {

        TradeConvergenceAnalysisResultResponse resultResponse = new TradeConvergenceAnalysisResultResponse();

        List<TradeConvergenceAnalysisResult> results;
        long total;
        if (request.getSearchType() == 0 && !CollectionUtils.isEmpty(request.getCardNums())) {

            Map<String, Object> map = convergenceAnalysisResultViaChosenMainCards(request, caseId);

            results = (List<TradeConvergenceAnalysisResult>) map.get("result");
            total = (long) map.get("total");
        } else {

            // TODO 全部条件
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
    private Map<String, Object> convergenceAnalysisResultViaAllMainCards(TradeConvergenceAnalysisQueryRequest request, String caseId) {

        // 由于无法一次性获取全部调单卡号, 因此批量获取调单卡号集合 // TODO 暂定为每次5000-10000
        boolean flag = true;
        List<TradeConvergenceAnalysisResult> allResults = new ArrayList<>();
        int page = request.getPageRequest().getPage();
        int size = request.getPageRequest().getPageSize();
        int from = 0;
        while (flag) {

            // TODO 当allResults 大于等于 一定量的时候,为了避免内存溢出(或者需要调正JVM的话),需要设置一个阈值
            // 循环获取调单卡号集合
            request.getPageRequest().setPage(from);
            request.getPageRequest().setPageSize(11850);
            List<String> mainCards = fundTacticsAnalysis.getAllMainCardsViaPageable(request, caseId);
            if (mainCards.size() < MAIN_CARD_SIZE) {
                flag = false;
            }
            // 调用 convergenceAnalysisResultViaChosenMainCards 方法获取结果
            // 设置调单卡号集合
            request.setCardNums(mainCards);
            Map<String, Object> map = convergenceAnalysisResultViaChosenMainCards(request, caseId);
            List<TradeConvergenceAnalysisResult> results = (List<TradeConvergenceAnalysisResult>) map.get("result");
            allResults.addAll(results);
            from += MAIN_CARD_SIZE;
        }
        // 重新设置page
        // 重新设置page 和 size
        request.getPageRequest().setPage(page);
        request.getPageRequest().setPageSize(size);
        // 进行内存的合并与排序
        Map<String, List<TradeConvergenceAnalysisResult>> groupResult = allResults.stream().collect(Collectors.groupingBy(TradeConvergenceAnalysisResult::fetchGroupKey));
        // 最终合并的结果
        List<TradeConvergenceAnalysisResult> mergeResult = new ArrayList<>(groupResult.size());
        groupResult.forEach((key, value) -> {

            if (value.size() > 1) {
                //
                mergeResult.add(TradeConvergenceAnalysisResult.mergeTradeConvergenceAnalysisResult(value));
            } else {
                TradeConvergenceAnalysisResult tradeConvergenceAnalysisResult = value.get(0);
                // 保留2位小数
                TradeConvergenceAnalysisResult.amountReservedTwo(tradeConvergenceAnalysisResult);
                mergeResult.add(tradeConvergenceAnalysisResult);
            }
        });
        // 排序与分页
        List<TradeConvergenceAnalysisResult> results = TradeConvergenceAnalysisResult.tradeConvergenceAnalysisResultSortAndPage(mergeResult, request.getPageRequest(), request.getSortRequest());
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("result", results);
        resultMap.put("total", Long.parseLong(String.valueOf(mergeResult.size())));
        return resultMap;
    }

    /**
     * <h2> 选择个体 / 选择部分调单卡号集合 </h2>
     */
    @SuppressWarnings("all")
    private Map<String, Object> convergenceAnalysisResultViaChosenMainCards(TradeConvergenceAnalysisQueryRequest request, String caseId) {

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

        // 设置同级聚合(计算总数据量)
        AggregationParams totalAgg = total(request);
        if (null != totalAgg) {
            convergenceAgg.addSiblingAggregation(totalAgg);
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
}
