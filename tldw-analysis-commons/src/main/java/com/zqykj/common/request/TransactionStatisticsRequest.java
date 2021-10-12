package com.zqykj.common.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: 交易统计请求体
 * @Author zhangkehou
 * @Date 2021/9/28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionStatisticsRequest implements Serializable {


    /**
     * 交易统计查询请求体
     */
    private TransactionStatisticsQuery transactionStatisticsQuery;


    /**
     * 交易统计聚合请求体
     */
    private TransactionStatisticsAggs transactionStatisticsAggs;

    /**
     * 案件编号
     */
    private String caseId;

}
