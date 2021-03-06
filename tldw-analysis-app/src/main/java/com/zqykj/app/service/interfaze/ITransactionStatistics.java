package com.zqykj.app.service.interfaze;

import com.alibaba.excel.ExcelWriter;
import com.zqykj.app.service.vo.fund.*;
import com.zqykj.app.service.vo.fund.middle.TradeAnalysisDetailResult;
import com.zqykj.common.request.TransactionStatisticsAggs;
import com.zqykj.common.response.HistogramStatisticResponse;
import com.zqykj.infrastructure.core.ServerResponse;


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
    TradeStatisticalAnalysisFundSumByDate getSummaryOfTradeAmountGroupedByTime(FundDateRequest request);

    /**
     * <h2> 获取交易统计分析结果 </h2>
     **/
    ServerResponse<FundAnalysisResultResponse<TradeStatisticalAnalysisResult>> tradeStatisticsAnalysisResult(TradeStatisticalAnalysisQueryRequest queryRequest, int from, int size,
                                                                                                             boolean isComputeTotal) throws Exception;

    /**
     * <h2> 获取交易统计分析结果详情 </h2>
     */
    ServerResponse<FundAnalysisResultResponse<TradeAnalysisDetailResult>> getDetail(FundTacticsPartGeneralRequest request);

    /**
     * <h2> 交易统计分析结果详情导出 </h2>
     */
    ServerResponse<String> detailExport(ExcelWriter excelWriter, FundTacticsPartGeneralRequest request) throws Exception;

    /**
     * <h2> 交易统计分析结果导出 </h2>
     */
    ServerResponse<String> transactionStatisticsAnalysisResultExport(ExcelWriter excelWriter, TradeStatisticalAnalysisQueryRequest request) throws Exception;
}
