/**
 * @作者 Mcj
 */
package com.zqykj.app.service.dao;

import com.zqykj.annotations.Query;
import com.zqykj.domain.test.BankTransactionFlowTest;
import com.zqykj.repository.ElasticsearchRepository;

import java.util.List;

public interface BankTransactionFlowTestDao extends ElasticsearchRepository<BankTransactionFlowTest, String> {

    @Query("{\"match_all\" : {}}")
    List<BankTransactionFlowTest> matchAll();
}
