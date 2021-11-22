package com.zqykj.app.service.interfaze.impl;

import com.zqykj.app.service.interfaze.IAssetTrendsTactics;
import com.zqykj.app.service.strategy.AggregateResultConversionAccessor;
import com.zqykj.common.enums.TacticsTypeEnum;
import com.zqykj.common.request.AssetTrendsRequest;
import com.zqykj.app.service.vo.fund.FundTacticsPartGeneralPreRequest;
import com.zqykj.common.request.TradeStatisticalAnalysisPreRequest;
import com.zqykj.common.response.AssetTrendsResponse;
import com.zqykj.domain.bank.BankTransactionFlow;
import com.zqykj.app.service.factory.AggregationRequestParamFactory;
import com.zqykj.app.service.factory.QueryRequestParamFactory;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.query.CombinationQueryParams;
import com.zqykj.parameters.query.QuerySpecialParams;
import com.zqykj.repository.EntranceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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

    private final AggregateResultConversionAccessor aggregateResultConversionAccessor;

    @Override
    public List<AssetTrendsResponse> accessAssetTrendsTacticsResult(String caseId, AssetTrendsRequest assetTrendsRequest) {
        /**
         * 资产趋势查询请求参数转换
         *
         * */
        TradeStatisticalAnalysisPreRequest fundTacticsPartGeneralPreRequest = assetTrendsRequest.convertFrom(assetTrendsRequest);

        FundTacticsPartGeneralPreRequest tacticsPartGeneralPreRequest = new FundTacticsPartGeneralPreRequest();
        BeanUtils.copyProperties(fundTacticsPartGeneralPreRequest, tacticsPartGeneralPreRequest);

        /**
         * elasticSearch特殊查询参数的构建.
         *
         * */
        CombinationQueryParams combinationQueryParams = queryRequestParamFactory.buildCommonQueryParamsViaBankTransactionFlow(tacticsPartGeneralPreRequest, caseId);
        QuerySpecialParams query = new QuerySpecialParams();
        query.addCombiningQueryParams(combinationQueryParams);
        /**
         * elasticSearch聚合参数的构建.
         * */
        AggregationParams aggs = aggregationRequestParamFactory.createAssetTrendsAnalysisQueryAgg(assetTrendsRequest);

        aggs.setResultName("AssetTrends");

        /**
         * elasticsearch返回的结果.
         * */
        Map<String, List<List<Object>>> result = entranceRepository.compoundQueryAndAgg(query, aggs, BankTransactionFlow.class, caseId);

        /**
         * 资产趋势结果解析,并将此结果返回给上层.
         * */
        List<AssetTrendsResponse> responses = (List<AssetTrendsResponse>) aggregateResultConversionAccessor.access(result.get(aggs.getResultName()), caseId, assetTrendsRequest, TacticsTypeEnum.ASSET_TRENDS);


        return responses;
    }
}
