/**
 * @作者 Mcj
 */
package com.zqykj.app.service.dao;

import com.zqykj.annotations.Query;
import com.zqykj.domain.bank.StandardBankTransactionFlow;
import com.zqykj.tldw.aggregate.searching.ElasticsearchTemplateOperations;

import java.util.List;

public interface ElasticTestDao extends ElasticsearchTemplateOperations<StandardBankTransactionFlow, String> {


    @Query("{\"bool\" : {\"must\" : {\"exists\" : {\"field\" : \"?0\"}}}}")
    List<StandardBankTransactionFlow> existsByField(String field);
}
