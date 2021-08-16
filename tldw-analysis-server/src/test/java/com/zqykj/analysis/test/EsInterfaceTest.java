/**
 * @作者 Mcj
 */
package com.zqykj.analysis.test;

import com.zqykj.app.service.dao.ElasticTestDao;
import com.zqykj.domain.bank.StandardBankTransactionFlow;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EsInterfaceTest {

    @Autowired
    private ElasticTestDao testDao;

    @Test
    public void context() {

    }

    @Test
    public void testEsInterface() throws Exception {
        System.out.println(testDao.search(new MatchAllQueryBuilder(), StandardBankTransactionFlow.class).size());
    }
}
