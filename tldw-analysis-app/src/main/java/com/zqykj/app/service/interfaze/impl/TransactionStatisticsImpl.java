package com.zqykj.app.service.interfaze.impl;

import com.zqykj.app.service.interfaze.ITransactionStatistics;
import com.zqykj.common.request.TransactionStatisticsRequest;
import com.zqykj.common.response.TransactionStatisticsResponse;
import org.springframework.stereotype.Service;

/**
 * @Description: 交易统计实现类
 * @Author zhangkehou
 * @Date 2021/9/28
 */
@Service
public class TransactionStatisticsImpl implements ITransactionStatistics {
    @Override
    public TransactionStatisticsResponse calculateStatisticalResults(TransactionStatisticsRequest transactionStatisticsRequest) {

        return null;
    }
}
