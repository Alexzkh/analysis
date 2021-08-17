package com.zqykj.app.service.operation;

import com.zqykj.domain.transaction.TransactionRecord;
import com.zqykj.tldw.aggregate.searching.esclientrhl.ElasticsearchOperations;

/**
 * @Description: test ElasticsearchOperarionsTempleate
 * @Author zhangkehou
 * @Date 2021/8/16
 */
public interface TransactionRecordOperations extends ElasticsearchOperations<TransactionRecord,String> {


}
