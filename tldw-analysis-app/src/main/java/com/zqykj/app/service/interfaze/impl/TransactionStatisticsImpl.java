package com.zqykj.app.service.interfaze.impl;

import com.zqykj.app.service.field.TacticsAnalysisField;
import com.zqykj.app.service.interfaze.ITransactionStatistics;
import com.zqykj.app.service.system.QueryBuilderExecutor;
import com.zqykj.app.service.transform.NumericalConversion;
import com.zqykj.app.service.vo.tarde_statistics.TradeStatisticalAnalysisQueryRequest;
import com.zqykj.common.constant.Constants;
import com.zqykj.common.enums.HistogramStatistic;
import com.zqykj.common.request.QueryParams;
import com.zqykj.app.service.vo.tarde_statistics.TimeGroupTradeAmountSum;
import com.zqykj.app.service.vo.tarde_statistics.TradeStatisticalAnalysisPreRequest;
import com.zqykj.common.request.TransactionStatisticsRequest;
import com.zqykj.common.response.HistogramStatisticResponse;
import com.zqykj.common.response.ParsedStats;
import com.zqykj.common.response.TransactionStatisticsResponse;
import com.zqykj.domain.Range;
import com.zqykj.domain.bank.BankTransactionFlow;
import com.zqykj.factory.AggregationRequestParamFactory;
import com.zqykj.factory.QueryRequestParamFactory;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.repository.EntranceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import com.zqykj.common.vo.TimeTypeRequest;
import com.zqykj.core.aggregation.factory.AggregateRequestFactory;
import com.zqykj.infrastructure.core.ServerResponse;
import com.zqykj.parameters.query.QuerySpecialParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description: 交易统计实现类
 * @Author zhangkehou
 * @Date 2021/9/28
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TransactionStatisticsImpl implements ITransactionStatistics {


    private final EntranceRepository entranceRepository;

    private final AggregationRequestParamFactory aggregationRequestParamFactory;

    private final QueryRequestParamFactory queryRequestParamFactory;


    @Override
    public TransactionStatisticsResponse calculateStatisticalResults(TransactionStatisticsRequest transactionStatisticsRequest) {

        return null;
    }

    @Override
    public HistogramStatisticResponse accessHistogramStatistics(TransactionStatisticsRequest transactionStatisticsRequest) {
        List<HistogramStatistic> responseList = new ArrayList<>();
        HistogramStatisticResponse histogramStatisticResponse = new HistogramStatisticResponse();
        try {


            /**
             * 构建交易统计查询条件.
             * */
            List<QueryParams> queryParams = QueryBuilderExecutor.buildTransactionStatisticsQuery(transactionStatisticsRequest);

            /**
             * 根据查询条件计算出当前数据中最大值.
             * */
            Map<String, ParsedStats> map = entranceRepository.statsAggs(queryParams, Constants.Individual.FOURTH_AGGREGATE_NAME,
                    transactionStatisticsRequest.getCaseId(), BankTransactionFlow.class);
            ParsedStats parsedStats = map.get(Constants.BucketName.STATS);
            Double max = parsedStats.getMax();

            /**
             * 然后根据最大值和传入的区间个数来获取range范围,从而作为直方图聚合参数range的入参.
             * */
            List<Range> ranges = NumericalConversion.intervalConversion(max, transactionStatisticsRequest.getTransactionStatisticsAggs().getHistorgramNumbers());

            /**
             * 根据range参数和构建好的queryParams做聚合统计算出日志直方图结果.
             * */
            Map queryResultMap = entranceRepository.rangeAggs(queryParams, Constants.Individual.FOURTH_AGGREGATE_NAME
                    , transactionStatisticsRequest.getCaseId(),
                    ranges, BankTransactionFlow.class);
            /**
             * 转换结果封装业务层数据给前台.
             * */
            queryResultMap.forEach((key, value) -> {
                HistogramStatistic histogramStatistic = HistogramStatistic.builder()
                        .abscissa((String) key)
                        .ordinate((Long) value)
                        .build();
                responseList.add(histogramStatistic);
            });
            histogramStatisticResponse.setHistogramStatisticList(responseList);
        } catch (Exception e) {
            log.error("获取柱状图统计结果失败：{}", e);
        }
        return histogramStatisticResponse;
    }

    @Override
    public QuerySpecialParams preQueryTransactionStatisticsAnalysis(String caseId, TradeStatisticalAnalysisPreRequest request) {
        // 构建查询参数
        return queryRequestParamFactory.createTradeAmountByTimeQuery(request, caseId);
    }

    @Override
    public ServerResponse<TimeGroupTradeAmountSum> getTradeAmountByTime(String caseId, TradeStatisticalAnalysisPreRequest request, TimeTypeRequest timeType) {

        // 构建查询参数
        QuerySpecialParams query = this.preQueryTransactionStatisticsAnalysis(caseId, request);
        // 构建  DateSpecificFormat对象
        Map<String, Object> result = entranceRepository.dateGroupAndSum(query, TacticsAnalysisField.TRADING_TIME,
                AggregateRequestFactory.convertFromTimeType(timeType.name()),
                TacticsAnalysisField.TRANSACTION_MONEY, BankTransactionFlow.class, caseId);

        TimeGroupTradeAmountSum groupTradeAmountSum = new TimeGroupTradeAmountSum();

        if (!CollectionUtils.isEmpty(result)) {

            Map<String, Object> resultNew = new LinkedHashMap<>();
            if (TimeTypeRequest.h == timeType) {
                // 需要对key 排序
                result.entrySet().stream()
                        .sorted(Comparator.comparing(x -> Integer.parseInt(x.getKey())))
                        .forEachOrdered(x -> resultNew.put(x.getKey(), x.getValue()));
                groupTradeAmountSum.setDates(resultNew.keySet());
                List<String> values = resultNew.values().stream().map(Object::toString).collect(Collectors.toList());
                groupTradeAmountSum.setTradeAmounts(values);
            } else {
                groupTradeAmountSum.setDates(result.keySet());
                List<String> values = result.values().stream().map(Object::toString).collect(Collectors.toList());
                groupTradeAmountSum.setTradeAmounts(values);
            }
        }

        return ServerResponse.createBySuccess(groupTradeAmountSum);
    }

    public ServerResponse getTransactionStatisticsAnalysisResult(String caseId, TradeStatisticalAnalysisQueryRequest queryRequest) {

        // 构建交易统计结果查询
        QuerySpecialParams query = queryRequestParamFactory.createTradeStatisticalAnalysisQueryRequest(caseId, queryRequest);

        // 构建交易统计分析聚合查询
        AggregationParams agg = aggregationRequestParamFactory.createTradeStatisticsAnalysisQueryAgg(queryRequest);

        Map<String, Object> result = entranceRepository.compoundQueryAndAgg(query, agg, BankTransactionFlow.class, caseId);

        return ServerResponse.createBySuccess(result);
    }

}
