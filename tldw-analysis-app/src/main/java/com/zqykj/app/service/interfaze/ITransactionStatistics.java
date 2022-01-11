package com.zqykj.app.service.interfaze;

import com.zqykj.app.service.vo.fund.*;
import com.zqykj.common.core.ServerResponse;
import com.zqykj.common.request.TransactionStatisticsDetailRequest;
import com.zqykj.common.request.TransactionStatisticsAggs;
import com.zqykj.common.response.HistogramStatisticResponse;
import com.zqykj.domain.Page;
import com.zqykj.domain.bank.BankTransactionFlow;

import java.util.concurrent.ExecutionException;


/**
 * @Description: 交易统计
 * @Author zhangkehou
 * @Date 2021/9/28
 */
public interface ITransactionStatistics {

    /**
     * 根据交易金额、入账金额、出账金额,获取交易统计直方图聚合统计结果.
     *
     * @param caseId:                    案件编号.
     * @param request:                   前置查询条件请求体.
     * @param transactionStatisticsAggs: 交易统计聚合统计请求体.
     * @return: com.zqykj.common.response.HistogramStatisticResponse
     **/
    HistogramStatisticResponse getHistogramStatistics(String caseId, FundTacticsPartGeneralPreRequest request, TransactionStatisticsAggs transactionStatisticsAggs);

    /**
     * <h2> 获取按时间类型汇总金额的折线图结果 </h2>
     */
    TradeStatisticalAnalysisFundSumByDate getSummaryOfTradeAmountGroupedByTime(String caseId, FundDateRequest request);

    /**
     * <h2> 获取交易统计分析结果 </h2>
     **/
    ServerResponse<FundAnalysisResultResponse<TradeStatisticalAnalysisResult>> getTransactionStatisticsAnalysisResult(String caseId, TradeStatisticalAnalysisQueryRequest queryRequest) throws Exception;

    /**
     * @param caseId:                             案件编号.
     * @param transactionStatisticsDetailRequest: 交易统计详情页请求体
     * @return: com.zqykj.domain.Page
     **/
    Page<BankTransactionFlow> accessTransactionStatisticDetail(String caseId, TransactionStatisticsDetailRequest transactionStatisticsDetailRequest) throws Exception;

}
