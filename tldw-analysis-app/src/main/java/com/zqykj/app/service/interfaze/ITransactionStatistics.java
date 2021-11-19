package com.zqykj.app.service.interfaze;

import com.zqykj.app.service.vo.fund.FundDateRequest;
import com.zqykj.app.service.vo.fund.FundTacticsPartGeneralPreRequest;
import com.zqykj.app.service.vo.fund.TradeStatisticalAnalysisFundSumByDate;
import com.zqykj.app.service.vo.fund.TradeStatisticalAnalysisQueryRequest;
import com.zqykj.common.core.ServerResponse;
import com.zqykj.common.request.TransactionStatisticsDetailRequest;
import com.zqykj.common.request.TransactionStatisticsAggs;
import com.zqykj.common.request.TransactionStatisticsRequest;
import com.zqykj.common.response.HistogramStatisticResponse;
import com.zqykj.common.response.TransactionStatisticsResponse;
import com.zqykj.domain.Page;
import com.zqykj.domain.bank.BankTransactionFlow;
import com.zqykj.parameters.query.QuerySpecialParams;

import java.util.concurrent.ExecutionException;


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
    HistogramStatisticResponse getHistogramStatistics(String caseId, FundTacticsPartGeneralPreRequest request, TransactionStatisticsAggs transactionStatisticsAggs);


    TradeStatisticalAnalysisFundSumByDate getSummaryOfTradeAmountGroupedByTime(String caseId, FundDateRequest request);

    /**
     * @param caseId:  案件编号.
     * @param request: 前置查询条件请求体.
     * @return: com.zqykj.parameters.query.QuerySpecialParams
     **/
    QuerySpecialParams preQueryTransactionStatisticsAnalysis(String caseId, FundTacticsPartGeneralPreRequest request);


    /**
     * 获取交易统计分析结果(主要是直方图数据、折线图数据以及行列数据).
     *
     * @param caseId:       案件编号.
     * @param queryRequest: 交易统计分析查询请求体.
     * @return: com.zqykj.common.core.ServerResponse
     **/
    ServerResponse getTransactionStatisticsAnalysisResult(String caseId, TradeStatisticalAnalysisQueryRequest queryRequest) throws ExecutionException, InterruptedException;

    /**
     * @param caseId:                             案件编号.
     * @param transactionStatisticsDetailRequest: 交易统计详情页请求体
     * @return: com.zqykj.domain.Page
     **/
    Page<BankTransactionFlow> accessTransactionStatisticDetail(String caseId, TransactionStatisticsDetailRequest transactionStatisticsDetailRequest) throws Exception;

}
