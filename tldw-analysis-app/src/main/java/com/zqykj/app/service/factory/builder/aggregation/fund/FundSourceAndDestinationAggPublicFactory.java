package com.zqykj.app.service.factory.builder.aggregation.fund;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zqykj.app.service.factory.AggregationResultEntityParseFactory;
import com.zqykj.app.service.factory.FundSourceAndDestinationAggRequestParamFactory;
import com.zqykj.app.service.factory.FundSourceAndDestinationQueryRequestFactory;
import com.zqykj.app.service.vo.fund.FundSourceAndDestinationBankRecord;
import com.zqykj.domain.bank.BankTransactionRecord;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.query.QuerySpecialParams;
import com.zqykj.repository.EntranceRepository;
import com.zqykj.util.JacksonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description: 资金来源去向聚合公共工厂
 * @Author zhangkehou
 * @Date 2021/11/23
 */

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FundSourceAndDestinationAggPublicFactory {

    private final FundSourceAndDestinationQueryRequestFactory queryRequestParamFactory;


    private final FundSourceAndDestinationAggRequestParamFactory aggregationRequestParamFactory;
    private final EntranceRepository entranceRepository;

    private final AggregationResultEntityParseFactory aggregationResultEntityParseFactory;

    public List<FundSourceAndDestinationBankRecord> accessTopN(AggregationParams aggregationParams , QuerySpecialParams querySpecialParams,String caseId){

        Map<String, List<List<Object>>> sourceResult = entranceRepository.compoundQueryAndAgg(querySpecialParams, aggregationParams, BankTransactionRecord.class, caseId);
        List<String> sourceOppositeTitles = new ArrayList<>(aggregationParams.getEntityAggColMapping().keySet());
        List<Map<String, Object>> localEntityMapping = aggregationResultEntityParseFactory.convertEntity(
                sourceResult.get(aggregationParams.getResultName()),sourceOppositeTitles,FundSourceAndDestinationBankRecord.class);
        // 来源实体数据组装
        List<FundSourceAndDestinationBankRecord> results = JacksonUtils.parse(JacksonUtils.toJson(localEntityMapping), new TypeReference<List<FundSourceAndDestinationBankRecord>>() {
        });

        return results;
    }
}
