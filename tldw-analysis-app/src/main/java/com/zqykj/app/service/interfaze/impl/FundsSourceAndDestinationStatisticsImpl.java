package com.zqykj.app.service.interfaze.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zqykj.app.service.interfaze.IFundsSourceAndDestinationStatistics;
import com.zqykj.app.service.interfaze.factory.AggregationRequestParamFactory;
import com.zqykj.app.service.interfaze.factory.AggregationResultEntityParseFactory;
import com.zqykj.app.service.interfaze.factory.QueryRequestParamFactory;
import com.zqykj.app.service.strategy.analysis.impl.FundSourceAndDestinationFactory;
import com.zqykj.app.service.vo.fund.*;
import com.zqykj.common.enums.FundsResultType;
import com.zqykj.common.request.FundSourceAndDestinationCardResultRequest;
import com.zqykj.common.request.FundsSourceAndDestinationStatisticsRequest;
import com.zqykj.common.response.FundsSourceAndDestinationPieChartStatisticsResponse;
import com.zqykj.common.response.FundsSourceAndDestinationStatisticsResponse;
import com.zqykj.common.response.FundsSourceAndDestinationTrendResponse;
import com.zqykj.common.vo.FundsSourceAndDestinationLineChart;
import com.zqykj.domain.bank.BankTransactionRecord;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.query.QuerySpecialParams;
import com.zqykj.repository.EntranceRepository;
import com.zqykj.util.JacksonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @Description: 资金来源去向战法业务接口
 * @Author zhangkehou
 * @Date 2021/11/6
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FundsSourceAndDestinationStatisticsImpl implements IFundsSourceAndDestinationStatistics {

    private final QueryRequestParamFactory queryRequestParamFactory;

    private final AggregationRequestParamFactory aggregationRequestParamFactory;

    private final EntranceRepository entranceRepository;

    private final AggregationResultEntityParseFactory aggregationResultEntityParseFactory;

    private final FundSourceAndDestinationFactory fundSourceAndDestinationFactory;

    @Override
    public FundSourceAndDestinationFlowDiagramResponse accessFundSourceAndDestinationTopN(FundsSourceAndDestinationStatisticsRequest request, String caseId) throws Exception {
       return fundSourceAndDestinationFactory.access(request.getFundsSourceAndDestinationStatisticsType()).accessFundSourceAndDestinationTopN(request,caseId);
    }

    @Override
    public FundsSourceAndDestinationTrendResponse accessFundSourceAndDestinationTrend(FundsSourceAndDestinationStatisticsRequest request, String caseId) throws Exception {
        return fundSourceAndDestinationFactory.access(request.getFundsSourceAndDestinationStatisticsType()).accessFundSourceAndDestinationTrend(request,caseId);
    }

    @Override
    public List<FundSourceAndDestinationResultList> accessFundSourceAndDestinationList(FundsSourceAndDestinationStatisticsRequest request, String caseId) throws Exception {
        return fundSourceAndDestinationFactory.access(request.getFundsSourceAndDestinationStatisticsType()).accessFundSourceAndDestinationList(request,caseId);
    }

    @Override
    public FundsSourceAndDestinationPieChartStatisticsResponse accessFundSourceAndDestinationPieChart(FundsSourceAndDestinationStatisticsRequest request, String caseId) throws Exception {
        return fundSourceAndDestinationFactory.access(request.getFundsSourceAndDestinationStatisticsType()).accessFundSourceAndDestinationPieChart(request,caseId);
    }

    @Override
    public List<FundSourceAndDestinationResultCardList> accessFundSourceAndDestinationCardList(FundSourceAndDestinationCardResultRequest request, String caseId) throws Exception {
        return fundSourceAndDestinationFactory.access(request.getFundsSourceAndDestinationStatisticsType()).accessFundSourceAndDestinationCardList(request,caseId);
    }

    @Override
    @Deprecated
    public FundsSourceAndDestinationStatisticsResponse accessFundsSourceAndDestinationStatisticsResult(FundsSourceAndDestinationStatisticsRequest fundsSourceAndDestinationStatisticsRequest, String caseId) {

        // 构建公共查询参数.
        QuerySpecialParams querySpecialParams = queryRequestParamFactory.buildFundsSourceAndDestinationAnalysisResquest(fundsSourceAndDestinationStatisticsRequest, caseId,null);

        //构建来源的聚合参数.
        AggregationParams sourceAggsParams = aggregationRequestParamFactory.buildFundsSourceTopNAgg(fundsSourceAndDestinationStatisticsRequest, FundsResultType.SOURCE);
        Map<String, List<List<Object>>> sourcedResult = entranceRepository.compoundQueryAndAgg(querySpecialParams, sourceAggsParams, BankTransactionRecord.class, caseId);
        List<String> sourceOppositeTitles = new ArrayList<>(sourceAggsParams.getMapping().keySet());
        List<Map<String, Object>> localEntityMapping = aggregationResultEntityParseFactory.convertEntity(
                aggregationResultEntityParseFactory.getColValueMapList(sourcedResult.get(sourceAggsParams.getResultName()), sourceOppositeTitles),
                sourceAggsParams.getEntityAggColMapping());
        // 来源实体数据组装
        List<FundSourceAndDestinationBankRecord> localResults = JacksonUtils.parse(JacksonUtils.toJson(localEntityMapping), new TypeReference<List<FundSourceAndDestinationBankRecord>>() {
        });


        // 构建去向的聚合参数
        AggregationParams destinationAggsParams = aggregationRequestParamFactory.buildFundsSourceTopNAgg(fundsSourceAndDestinationStatisticsRequest, FundsResultType.DESTINATION);

        Map<String, List<List<Object>>> destinationresult = entranceRepository.compoundQueryAndAgg(querySpecialParams, destinationAggsParams, BankTransactionRecord.class, caseId);
        List<String> destinationOppositeTitles = new ArrayList<>(sourceAggsParams.getMapping().keySet());
        List<Map<String, Object>> destinationEntityMapping = aggregationResultEntityParseFactory.convertEntity(
                aggregationResultEntityParseFactory.getColValueMapList(destinationresult.get(sourceAggsParams.getResultName()), destinationOppositeTitles),
                sourceAggsParams.getEntityAggColMapping());
        // 来源实体数据组装
        List<FundSourceAndDestinationBankRecord> destinationResults = JacksonUtils.parse(JacksonUtils.toJson(destinationEntityMapping), new TypeReference<List<FundSourceAndDestinationBankRecord>>() {
        });


        AggregationParams lineChartAggsParams = aggregationRequestParamFactory.buildFundsSourceAndDestinationLineChartAgg(fundsSourceAndDestinationStatisticsRequest,null);
        lineChartAggsParams.setResultName("fundsSourceAndDestinationLineChart");

        /**
         * elasticsearch返回的结果.
         * */
        Map<String, List<List<Object>>> result = entranceRepository.compoundQueryAndAgg(querySpecialParams, lineChartAggsParams, BankTransactionRecord.class, caseId);
        List<String> lineChartTitles = new ArrayList<>(lineChartAggsParams.getMapping().keySet());
        List<Map<String, Object>> lineChartEntityMapping = aggregationResultEntityParseFactory.convertEntity(
                aggregationResultEntityParseFactory.getColValueMapList(result.get(lineChartAggsParams.getResultName()), lineChartTitles),
                lineChartAggsParams.getEntityAggColMapping());
        // 来源实体数据组装
        List<FundSourceAndDestinationLineChart> lineChartResults = JacksonUtils.parse(JacksonUtils.toJson(lineChartEntityMapping), new TypeReference<List<FundSourceAndDestinationLineChart>>() {
        });

        //根据过滤条件把日期桶结果 分为来源和去向

        //来源趋势折线图数据
        List<FundsSourceAndDestinationLineChart> source = lineChartResults.stream().map(e -> {
                    if (e.getCreditsTimes() != 0) {
                        return new FundsSourceAndDestinationLineChart(e.getTradingTime(), e.getCreditsAmount());
                    }
                    return null;
                }

        ).collect(Collectors.toList());

        // 去向趋势折线图数据
        List<FundsSourceAndDestinationLineChart> destination = lineChartResults.stream().map(e -> {
                    if (e.getPayOutTimes() != 0) {
                        return new FundsSourceAndDestinationLineChart(e.getTradingTime(), e.getCreditsAmount());
                    }
                    return null;
                }

        ).collect(Collectors.toList());


        return null;
    }



}
