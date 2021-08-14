/**
 * @作者 Mcj
 */
package com.zqykj.app.service.dao;

import com.zqykj.domain.bank.StandardBankTransactionFlow;
import com.zqykj.tldw.aggregate.searching.ElasticsearchTemplateOperations;
import org.springframework.stereotype.Repository;

public interface ElasticTestDao extends ElasticsearchTemplateOperations<StandardBankTransactionFlow, String> {


}
