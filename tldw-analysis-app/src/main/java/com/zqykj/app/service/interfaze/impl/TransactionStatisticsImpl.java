package com.zqykj.app.service.interfaze.impl;

import com.zqykj.app.service.field.TacticsAnalysisField;
import com.zqykj.app.service.interfaze.ITransactionStatistics;
import com.zqykj.app.service.transform.NumericalConversion;
import com.zqykj.app.service.vo.tarde_statistics.TradeStatisticalAnalysisQueryRequest;
import com.zqykj.common.constant.Constants;
import com.zqykj.common.core.ServerResponse;
import com.zqykj.common.enums.HistogramStatistic;
import com.zqykj.common.request.TransactionStatisticsDetailRequest;
import com.zqykj.common.response.*;
import com.zqykj.common.request.TradeStatisticalAnalysisPreRequest;
import com.zqykj.common.request.TransactionStatisticsAggs;
import com.zqykj.common.request.TransactionStatisticsRequest;
import com.zqykj.domain.*;
import com.zqykj.domain.bank.BankTransactionFlow;
import com.zqykj.factory.AggregationRequestParamFactory;
import com.zqykj.factory.QueryRequestParamFactory;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.repository.EntranceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import com.zqykj.common.vo.TimeTypeRequest;
import com.zqykj.core.aggregation.factory.AggregateRequestFactory;
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
    public TransactionStatisticsResponse calculateStatisticalResults(String caseId, TransactionStatisticsRequest transactionStatisticsRequest) {
        TradeStatisticalAnalysisPreRequest tradeStatisticalAnalysisPreRequest = transactionStatisticsRequest.getTradeStatisticalAnalysisPreRequest();
        TransactionStatisticsAggs transactionStatisticsAggs = transactionStatisticsRequest.getTransactionStatisticsAggs();
        TimeTypeRequest timeTypeRequest = transactionStatisticsAggs.getDateType();

        /**
         * 获取交易金额聚合统计直方图结果.
         * */
        HistogramStatisticResponse histogramStatisticResponse = this.getHistogramStatistics(caseId, tradeStatisticalAnalysisPreRequest, transactionStatisticsAggs);

        /**
         * 获取日期折现图聚合统计结果.
         * */
        TimeGroupTradeAmountSum timeGroupTradeAmountSum = this.getTradeAmountByTime(caseId, tradeStatisticalAnalysisPreRequest, timeTypeRequest);


        // todo 获取卡聚合统计列表

        /**
         * 交易统计返回结果封装.
         * */
        TransactionStatisticsResponse transactionStatisticsResponse = new TransactionStatisticsResponse(histogramStatisticResponse, timeGroupTradeAmountSum, null);


        return transactionStatisticsResponse;
    }


    @Override
    public HistogramStatisticResponse getHistogramStatistics(String caseId, TradeStatisticalAnalysisPreRequest request, TransactionStatisticsAggs transactionStatisticsAggs) {
        List<HistogramStatistic> responseList = new ArrayList<>();
        HistogramStatisticResponse histogramStatisticResponse = new HistogramStatisticResponse();

        // 构建查询参数
        QuerySpecialParams query = this.preQueryTransactionStatisticsAnalysis(caseId, request);
        /**
         * 根据查询条件计算出当前数据中最大值.
         * */
        Map<String, ParsedStats> map = entranceRepository.statsAggs(query, Constants.Individual.FOURTH_AGGREGATE_NAME,
                caseId, BankTransactionFlow.class);
        ParsedStats parsedStats = map.get(Constants.BucketName.STATS);
        Double max = parsedStats.getMax();

        /**
         * 然后根据最大值和传入的区间个数来获取range范围,从而作为直方图聚合参数range的入参.
         * */
        List<Range> ranges = NumericalConversion.intervalConversion(max, transactionStatisticsAggs.getHistorgramNumbers());

        /**
         * 根据range参数和构建好的queryParams做聚合统计算出日志直方图结果.
         * */
        Map queryResultMap = entranceRepository.rangeAggs(query, Constants.Individual.FOURTH_AGGREGATE_NAME
                , caseId,
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

        return histogramStatisticResponse;

    }

    @Override
    public QuerySpecialParams preQueryTransactionStatisticsAnalysis(String caseId, TradeStatisticalAnalysisPreRequest request) {
        // 构建查询参数
        return queryRequestParamFactory.createTradeAmountByTimeQuery(request, caseId);
    }

    @Override
    public Page<BankTransactionFlow> accessTransactionStatisticDetail(String caseId, TransactionStatisticsDetailRequest transactionStatisticsDetailRequest) throws Exception {


        PageRequest pageRequest = PageRequest.of(transactionStatisticsDetailRequest.getQueryRequest().getPaging().getPage(),
                transactionStatisticsDetailRequest.getQueryRequest().getPaging().getPageSize(),
                transactionStatisticsDetailRequest.getQueryRequest().getSorting().getOrder().isAscending() ? Sort.Direction.ASC : Sort.Direction.DESC,
                transactionStatisticsDetailRequest.getQueryRequest().getSorting().getProperty());
        Page<BankTransactionFlow> page = entranceRepository.findByCondition(pageRequest, caseId, BankTransactionFlow.class
                , transactionStatisticsDetailRequest.getCardNumber(), caseId, transactionStatisticsDetailRequest.getQueryRequest().getKeyword());
        return page;

    }

    @Override
    public TimeGroupTradeAmountSum getTradeAmountByTime(String caseId, TradeStatisticalAnalysisPreRequest request, TimeTypeRequest timeType) {

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

        return groupTradeAmountSum;
    }

    public ServerResponse getTransactionStatisticsAnalysisResult(String caseId, TradeStatisticalAnalysisQueryRequest queryRequest) {

        // 构建交易统计结果查询
        QuerySpecialParams query = queryRequestParamFactory.createTradeStatisticalAnalysisQueryRequest(queryRequest, caseId);

        // 构建交易统计分析聚合查询
        AggregationParams agg = aggregationRequestParamFactory.createTradeStatisticsAnalysisQueryAgg(queryRequest);

        Map<String, Object> result = entranceRepository.compoundQueryAndAgg(query, agg, BankTransactionFlow.class, caseId);

        return ServerResponse.createBySuccess(result);
    }

}
