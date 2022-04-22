package com.zqykj.app.service.strategy.analysis.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zqykj.app.service.factory.*;
import com.zqykj.app.service.factory.builder.aggregation.fund.FundSourceAndDestinationAggPublicFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.strategy.analysis.FundSourceAndDestinationStrategy;
import com.zqykj.app.service.vo.fund.*;
import com.zqykj.common.enums.FundsResultType;
import com.zqykj.common.enums.FundsSourceAndDestinationStatisticsType;
import com.zqykj.common.request.FundSourceAndDestinationCardResultRequest;
import com.zqykj.common.request.FundsSourceAndDestinationStatisticsRequest;
import com.zqykj.common.response.FundsSourceAndDestinationPieChartStatisticsResponse;
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
 * @Description: 资金来源去向--按照交易净和获取topN数据、趋势折线图数据、列表数据以及饼图数据。
 * @Author zhangkehou
 * @Date 2021/11/18
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TransactionNetSumStrategyImpl implements FundSourceAndDestinationStrategy {

    private final FundSourceAndDestinationQueryRequestFactory queryRequestParamFactory;

    private final FundSourceAndDestinationAggRequestParamFactory aggregationRequestParamFactory;

    private final EntranceRepository entranceRepository;

    private final AggregationResultEntityParseFactory aggregationResultEntityParseFactory;

    private final FundSourceAndDestinationAggPublicFactory fundSourceAndDestinationAggPublicFactory;

    @Override
    public FundSourceAndDestinationFlowDiagramResponse accessFundSourceAndDestinationTopN(FundsSourceAndDestinationStatisticsRequest fundsSourceAndDestinationStatisticsRequest, String caseId) {
        // 构建公共查询参数.
        QuerySpecialParams querySpecialParams = queryRequestParamFactory.buildFundsSourceAndDestinationAnalysisResquest(fundsSourceAndDestinationStatisticsRequest, caseId, FundsResultType.SOURCE);
        // 构建来源的聚合参数.
        AggregationParams sourceAggsParams = aggregationRequestParamFactory.buildFundsSourceTopNAgg(fundsSourceAndDestinationStatisticsRequest, FundsResultType.SOURCE);
        // 获取来源的结果
        List<FundSourceAndDestinationBankRecord> localResults = fundSourceAndDestinationAggPublicFactory.accessTopN(sourceAggsParams, querySpecialParams, caseId);
        // 构建去向的聚合参数
        AggregationParams destinationAggsParams = aggregationRequestParamFactory.buildFundsSourceTopNAgg(fundsSourceAndDestinationStatisticsRequest, FundsResultType.DESTINATION);
        // 去向实体数据组装
        List<FundSourceAndDestinationBankRecord> destinationResults =fundSourceAndDestinationAggPublicFactory.accessTopN(destinationAggsParams, querySpecialParams, caseId);
        return FundSourceAndDestinationFlowDiagramResponse.builder()
                .localResults(localResults)
                .destinationResults(destinationResults)
                .build();
    }

    @Override
    public FundsSourceAndDestinationTrendResponse accessFundSourceAndDestinationTrend(FundsSourceAndDestinationStatisticsRequest fundsSourceAndDestinationStatisticsRequest, String caseId) {
        // 构建公共查询参数.
        QuerySpecialParams querySpecialParams = queryRequestParamFactory.buildFundsSourceAndDestinationLineChartResquest(fundsSourceAndDestinationStatisticsRequest, caseId);

        AggregationParams lineChartAggsParams = aggregationRequestParamFactory.buildFundsSourceAndDestinationLineChartAgg(fundsSourceAndDestinationStatisticsRequest, FundsResultType.SOURCE);
        lineChartAggsParams.setResultName("sourceFundsSourceAndDestinationLineChart");

        /**
         * elasticsearch返回的结果.
         * */
        Map<String, List<List<Object>>> result = entranceRepository.compoundQueryAndAgg(querySpecialParams, lineChartAggsParams, BankTransactionRecord.class, caseId);
        List<String> lineChartTitles = new ArrayList<>(lineChartAggsParams.getEntityAggColMapping().keySet());
        List<Map<String, Object>> lineChartEntityMapping = aggregationResultEntityParseFactory.convertEntity(
                result.get(lineChartAggsParams.getResultName()), lineChartTitles, fundsSourceAndDestinationStatisticsRequest
                        .getFundsSourceAndDestinationStatisticsType()
                        .equals(FundsSourceAndDestinationStatisticsType.TRANSACTION_AMOUNT) ?
                        FundSourceAndDestinationLineChart.class :
                        FundSourceAndDestinationNetLineChart.class
        );
        // 来源实体数据组装
        List<FundSourceAndDestinationLineChart> lineChartResults = JacksonUtils.parse(JacksonUtils.toJson(lineChartEntityMapping), new TypeReference<List<FundSourceAndDestinationLineChart>>() {
        });
        AggregationParams destinationLineChartAggsParams = aggregationRequestParamFactory.buildFundsSourceAndDestinationLineChartAgg(fundsSourceAndDestinationStatisticsRequest, FundsResultType.DESTINATION);
        destinationLineChartAggsParams.setResultName("destinationFundsSourceAndDestinationLineChart");

        /**
         * elasticsearch返回的结果.
         * */
        Map<String, List<List<Object>>> destinationResult = entranceRepository.compoundQueryAndAgg(querySpecialParams, destinationLineChartAggsParams, BankTransactionRecord.class, caseId);
        List<String> destinationChartTitles = new ArrayList<>(destinationLineChartAggsParams.getEntityAggColMapping().keySet());
        List<Map<String, Object>> destinationLineChartEntityMapping = aggregationResultEntityParseFactory.convertEntity(
                destinationResult.get(destinationLineChartAggsParams.getResultName()), destinationChartTitles, fundsSourceAndDestinationStatisticsRequest
                        .getFundsSourceAndDestinationStatisticsType()
                        .equals(FundsSourceAndDestinationStatisticsType.TRANSACTION_AMOUNT) ?
                        FundSourceAndDestinationLineChart.class :
                        FundSourceAndDestinationNetLineChart.class
        );
        // 来源实体数据组装
        List<FundSourceAndDestinationLineChart> destinationLineChartResults = JacksonUtils.parse(JacksonUtils.toJson(destinationLineChartEntityMapping), new TypeReference<List<FundSourceAndDestinationLineChart>>() {
        });

        //根据过滤条件把日期桶结果 分为来源和去向

        //来源趋势折线图数据
        List<FundsSourceAndDestinationLineChart> source = lineChartResults.stream().map(e -> {
                    return new FundsSourceAndDestinationLineChart(e.getTradingTime(), e.getPayOutAmount());
                }

        ).collect(Collectors.toList());

        // 去向趋势折线图数据
        List<FundsSourceAndDestinationLineChart> destination = destinationLineChartResults.stream().map(e -> {

                    return new FundsSourceAndDestinationLineChart(e.getTradingTime(), e.getPayOutAmount());

                }
        ).collect(Collectors.toList());


        return FundsSourceAndDestinationTrendResponse.builder()
                .sourceLineCharts(source)
                .destinationLineCharts(destination)
                .build();
    }

    @Override
    public List<FundSourceAndDestinationResultList> accessFundSourceAndDestinationList(FundsSourceAndDestinationStatisticsRequest fundsSourceAndDestinationStatisticsRequest, String caseId) {
        // 构建公共查询参数.
        QuerySpecialParams querySpecialParams = queryRequestParamFactory.buildFundsSourceAndDestinationAnalysisResquest(fundsSourceAndDestinationStatisticsRequest, caseId,
                fundsSourceAndDestinationStatisticsRequest.getFundsResultType());

        // 构建聚合查询参数.
        AggregationParams aggregationParams = aggregationRequestParamFactory.buildFundsSourceAndDestinationResultListAgg(fundsSourceAndDestinationStatisticsRequest);

        Map<String, List<List<Object>>> result = entranceRepository.compoundQueryAndAgg(querySpecialParams, aggregationParams, BankTransactionRecord.class, caseId);
        List<String> resultListTitles = new ArrayList<>(aggregationParams.getEntityAggColMapping().keySet());
        List<Map<String, Object>> resultListEntityMapping = aggregationResultEntityParseFactory.convertEntity(
                result.get(aggregationParams.getResultName()), resultListTitles, FundSourceAndDestinationResultList.class
        );
        // 来源实体数据组装
        List<FundSourceAndDestinationResultList> fundsResultList = JacksonUtils.parse(JacksonUtils.toJson(resultListEntityMapping), new TypeReference<List<FundSourceAndDestinationResultList>>() {
        });
        return fundsResultList;
    }

    @Override
    public FundsSourceAndDestinationPieChartStatisticsResponse accessFundSourceAndDestinationPieChart(FundsSourceAndDestinationStatisticsRequest fundsSourceAndDestinationStatisticsRequest, String caseId) {

        QuerySpecialParams querySpecialParams = queryRequestParamFactory.buildFundsSourceAndDestinationAnalysisResquest(fundsSourceAndDestinationStatisticsRequest, caseId,
                FundsResultType.SOURCE);

        AggregationParams aggregationParams = aggregationRequestParamFactory.buildFundsSourceAndDestinationPieChartAgg(fundsSourceAndDestinationStatisticsRequest, FundsResultType.SOURCE);
        Map<String, List<List<Object>>> result = entranceRepository.compoundQueryAndAgg(querySpecialParams, aggregationParams, BankTransactionRecord.class, caseId);
        List<List<Object>> sourceResult = result.get(FundTacticsAnalysisField.PIPLINE_SUM_BUCKET);

        AggregationParams destinationParams = aggregationRequestParamFactory.buildFundsSourceAndDestinationPieChartAgg(fundsSourceAndDestinationStatisticsRequest, FundsResultType.DESTINATION);
        Map<String, List<List<Object>>> destinationResult = entranceRepository.compoundQueryAndAgg(querySpecialParams, destinationParams, BankTransactionRecord.class, caseId);
        List<List<Object>> destResult = destinationResult.get(FundTacticsAnalysisField.PIPLINE_TRANSACTION_NET_NUMBER);

        return transferPieChartResult(sourceResult, destResult);
    }

    @Override
    public List<FundSourceAndDestinationResultCardList> accessFundSourceAndDestinationCardList(FundsSourceAndDestinationStatisticsRequest fundSourceAndDestinationCardResultRequest, String caseId) {


        // 构建公共查询参数.
        QuerySpecialParams querySpecialParams = queryRequestParamFactory.buildFundsSourceAndDestinationCardResultResquest(fundSourceAndDestinationCardResultRequest, caseId);

        AggregationParams aggregationParams = aggregationRequestParamFactory.buildFundsSourceAndDestinationCardResultListAgg(fundSourceAndDestinationCardResultRequest);
        aggregationParams.setResultName("FundSourceAndDestinationCardLis");
        Map<String, List<List<Object>>> result = entranceRepository.compoundQueryAndAgg(querySpecialParams, aggregationParams, BankTransactionRecord.class, caseId);
        List<String> resultListTitles = new ArrayList<>(aggregationParams.getEntityAggColMapping().keySet());
        List<Map<String, Object>> resultListEntityMapping = aggregationResultEntityParseFactory.convertEntity(
                result.get(aggregationParams.getResultName()), resultListTitles, FundSourceAndDestinationResultCardList.class
        );
        // 来源实体数据组装
        List<FundSourceAndDestinationResultCardList> fundsResultList = JacksonUtils.parse(JacksonUtils.toJson(resultListEntityMapping), new TypeReference<List<FundSourceAndDestinationResultCardList>>() {
        });

        return fundsResultList;
    }


}
