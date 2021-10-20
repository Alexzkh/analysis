package com.zqykj.app.service.interfaze.impl;

import com.zqykj.app.service.interfaze.IAssetTrendsTactics;
import com.zqykj.common.request.AssetTrendsRequest;
import com.zqykj.common.request.TradeStatisticalAnalysisPreRequest;
import com.zqykj.common.response.AssetTrendsResponse;
import com.zqykj.domain.bank.BankTransactionFlow;
import com.zqykj.factory.AggregationRequestParamFactory;
import com.zqykj.factory.QueryRequestParamFactory;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.query.QuerySpecialParams;
import com.zqykj.repository.EntranceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @Description: 资产趋势战法实现类.
 * @Author zhangkehou
 * @Date 2021/10/19
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AssetTrendsTacticsImpl implements IAssetTrendsTactics {


    private final EntranceRepository entranceRepository;

    private final AggregationRequestParamFactory aggregationRequestParamFactory;

    private final QueryRequestParamFactory queryRequestParamFactory;

    @Override
    public AssetTrendsResponse accessAssetTrendsTacticsResult(String caseId, AssetTrendsRequest assetTrendsRequest) {
        AssetTrendsResponse assetTrendsResponse = new AssetTrendsResponse();
        TradeStatisticalAnalysisPreRequest tradeStatisticalAnalysisPreRequest = assetTrendsRequest.convertFrom(assetTrendsRequest);
        QuerySpecialParams query = queryRequestParamFactory.buildCommonQuerySpecialParams(tradeStatisticalAnalysisPreRequest,caseId);
        AggregationParams aggs = aggregationRequestParamFactory.createAssetTrendsAnalysisQueryAgg(assetTrendsRequest);
        Map<String, Object> result = entranceRepository.compoundQueryAndAgg(query, aggs, BankTransactionFlow.class, caseId);
        return assetTrendsResponse;
    }
}
