package com.zqykj.app.service.interfaze;

import com.zqykj.common.response.TimeGroupTradeAmountSum;
import com.zqykj.common.request.TradeStatisticalAnalysisPreRequest;
import com.zqykj.common.request.TransactionStatisticsAggs;
import com.zqykj.common.request.TransactionStatisticsRequest;
import com.zqykj.common.response.HistogramStatisticResponse;
import com.zqykj.common.response.TransactionStatisticsResponse;
import com.zqykj.common.vo.TimeTypeRequest;
import com.zqykj.parameters.query.QuerySpecialParams;


/**
 * @Description: 交易统计
 * @Author zhangkehou
 * @Date 2021/9/28
 */
public interface ITransactionStatistics {


    /**
     * 交易统计计算结果.
     *
     * @param transactionStatisticsRequest: 交易统计的请求体.
     * @return: com.zqykj.common.response。TransactionStatisticsResponse
     **/
    TransactionStatisticsResponse calculateStatisticalResults(String caseId, TransactionStatisticsRequest transactionStatisticsRequest);

    /**
     * 根据交易金额、入账金额、出账金额,获取交易统计直方图聚合统计结果.
     *
     * @param caseId:                    案件编号.
     * @param request:                   前置查询条件请求体.
     * @param transactionStatisticsAggs: 交易统计聚合统计请求体.
     * @return: com.zqykj.common.response.HistogramStatisticResponse
     **/
    HistogramStatisticResponse getHistogramStatistics(String caseId, TradeStatisticalAnalysisPreRequest request, TransactionStatisticsAggs transactionStatisticsAggs);

    /**
     * 根据年、月、日、时聚合统计，获取折线图展示数据
     *
     * @param caseId:   案件编号.
     * @param request:  前置查询条件请求体.
     * @param timeType: 交易统计折现图根据日期
     * @return: com.zqykj.common.response.TimeGroupTradeAmountSum
     **/
    TimeGroupTradeAmountSum getTradeAmountByTime(String caseId, TradeStatisticalAnalysisPreRequest request, TimeTypeRequest timeType);

    /**
     * @param caseId:  案件编号.
     * @param request: 前置查询条件请求体.
     * @return: com.zqykj.parameters.query.QuerySpecialParams
     **/
    QuerySpecialParams preQueryTransactionStatisticsAnalysis(String caseId, TradeStatisticalAnalysisPreRequest request);


}
