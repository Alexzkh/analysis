/**
 * @作者 Mcj
 */
package com.zqykj.app.service.interfaze.impl;

import com.zqykj.app.service.interfaze.IFundTacticsAnalysis;
import com.zqykj.app.service.factory.AggregationEntityMappingFactory;
import com.zqykj.app.service.factory.AggregationRequestParamFactory;
import com.zqykj.app.service.factory.QueryRequestParamFactory;
import com.zqykj.app.service.vo.fund.FundTacticsPartGeneralPreRequest;
import com.zqykj.domain.bank.BankTransactionFlow;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.query.QuerySpecialParams;
import com.zqykj.repository.EntranceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FundTacticsAnalysisImpl implements IFundTacticsAnalysis {

    private final EntranceRepository entranceRepository;

    private final AggregationRequestParamFactory aggregationRequestParamFactory;

    private final QueryRequestParamFactory queryRequestParamFactory;

    private final AggregationEntityMappingFactory aggregationEntityMappingFactory;

    /**
     * <h2> 批量获取调单卡号集合 </h2>
     */
    public List<String> getAllMainCardsViaPageable(FundTacticsPartGeneralPreRequest request, int from, int size, String caseId) {

        // 构建查询参数
        QuerySpecialParams query = queryRequestParamFactory.buildBasicParamQueryViaCase(request, caseId);

        // 构建查询卡号去重聚合查询
        AggregationParams groupQueryCard =
                aggregationRequestParamFactory.buildGetCardNumsInBatchesAgg(from, size);
        // 聚合名称-属性映射(为了聚合对应聚合名称下的聚合值)
        Map<String, String> mapping = aggregationEntityMappingFactory.buildGetCardNumsInBatchesAggMapping();
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

        AggregationParams distinctQueryCard = aggregationRequestParamFactory.getCardNumsTotal(request);

        Map<String, String> mapping = aggregationEntityMappingFactory.buildGetCardNumsTotalAggMapping();

        distinctQueryCard.setMapping(mapping);
        // 定义该聚合的功能名称
        distinctQueryCard.setResultName("distinctQueryCard");

        Map<String, List<List<Object>>> mainCardTotalResults = entranceRepository.compoundQueryAndAgg(query, distinctQueryCard, BankTransactionFlow.class, caseId);
        List<List<Object>> mainCardsTotal = mainCardTotalResults.get("distinctQueryCard");
        // 返回总量
        return Integer.parseInt(mainCardsTotal.get(0).get(0).toString());
    }
}
