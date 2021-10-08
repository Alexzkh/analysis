package com.zqykj.app.service.interfaze;

import com.zqykj.common.request.TransactionStatisticsRequest;
import com.zqykj.common.response.TransactionStatisticsResponse;

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
}
