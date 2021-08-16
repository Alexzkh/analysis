/**
 * @作者 Mcj
 */
package com.zqykj.controller;

import com.zqykj.domain.bank.StandardBankTransactionFlow;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.zqykj.app.service.dao.ElasticTestDao;

@RestController
public class TestController {

    @Autowired
    private ElasticTestDao testDao;


    @GetMapping("/test")
    public int test() throws Exception {
        return testDao.search(new MatchAllQueryBuilder(), StandardBankTransactionFlow.class).size();
    }
}
