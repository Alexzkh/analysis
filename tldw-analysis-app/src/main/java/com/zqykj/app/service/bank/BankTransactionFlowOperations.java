package com.zqykj.app.service.bank;

import com.zqykj.domain.bank.BankTransactionFlow;
import com.zqykj.tldw.aggregate.searching.esclientrhl.ElasticsearchOperations;

/**
 * @Description: Bank transaction flow operations d
 * @Author zhangkehou
 * @Date 2021/8/20
 */
public interface BankTransactionFlowOperations extends ElasticsearchOperations<BankTransactionFlow,Long> {

}
