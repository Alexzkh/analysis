package com.zqykj.app.service.interfaze;

import com.zqykj.app.service.vo.tarde_statistics.TimeGroupTradeAmountSum;
import com.zqykj.common.request.TradeStatisticalAnalysisPreRequest;
import com.zqykj.common.request.TransactionStatisticsAggs;
import com.zqykj.common.request.TransactionStatisticsRequest;
import com.zqykj.common.response.HistogramStatisticResponse;
import com.zqykj.common.response.TransactionStatisticsResponse;
import com.zqykj.common.vo.TimeTypeRequest;
import com.zqykj.infrastructure.core.ServerResponse;
import com.zqykj.parameters.query.QuerySpecialParams;


/**
 * @Description: 交易统计
 * @Author zhangkehou
 * @Date 2021/9/28
 */
public interface ITransactionStatistics {


    /**
     * 交易统计计算结果
     *
     * @param transactionStatisticsRequest: 交易统计的请求体
     * @return: com.zqykj.common.response。TransactionStatisticsResponse
     **/
    TransactionStatisticsResponse calculateStatisticalResults(TransactionStatisticsRequest transactionStatisticsRequest);


    HistogramStatisticResponse accessHistogramStatistics(TransactionStatisticsRequest transactionStatisticsRequest);

    HistogramStatisticResponse getHistogramStatistics(String caseId , TradeStatisticalAnalysisPreRequest request, TransactionStatisticsAggs transactionStatisticsAggs);

    ServerResponse<TimeGroupTradeAmountSum> getTradeAmountByTime(String caseId, TradeStatisticalAnalysisPreRequest request, TimeTypeRequest timeType);

    /**
     * <h2> 交易统计分析前置查询 </h2>
     */
    QuerySpecialParams preQueryTransactionStatisticsAnalysis(String caseId, TradeStatisticalAnalysisPreRequest request);
}
