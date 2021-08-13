/**
 * @作者 Mcj
 */
package com.zqykj.analysis.controller;

import com.zqykj.app.service.dao.ElasticTestDao;
import com.zqykj.domain.bank.StandardBankTransactionFlow;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    private ElasticTestDao testDao;

    @GetMapping("/test")
    public int test() throws Exception {
        return testDao.search(new MatchAllQueryBuilder(), StandardBankTransactionFlow.class).size();
    }
}
