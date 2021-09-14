package com.zqykj.app.service.operation;

import com.zqykj.domain.transaction.TransactionRecord;
import com.zqykj.repository.ElasticsearchRepository;

/**
 * @Description: test ElasticsearchOperarionsTempleate
 * @Author zhangkehou
 * @Date 2021/8/16
 */
public interface TransactionRecordOperations extends ElasticsearchRepository<TransactionRecord,String> {


}
