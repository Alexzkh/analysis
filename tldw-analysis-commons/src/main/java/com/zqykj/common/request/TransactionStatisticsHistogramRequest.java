package com.zqykj.common.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Description: 交易统计请求体
 * @Author zhangkehou
 * @Date 2021/9/28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionStatisticsHistogramRequest implements Serializable {


    /**
     * 交易统计查询请求体
     */
    private TradeStatisticalAnalysisPreRequest transactionStatisticsQuery;


    /**
     * 交易统计聚合请求体
     */
    private TransactionStatisticsAggs transactionStatisticsAggs;


}
