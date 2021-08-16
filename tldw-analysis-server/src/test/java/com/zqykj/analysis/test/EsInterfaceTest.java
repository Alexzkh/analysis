/**
 * @作者 Mcj
 */
package com.zqykj.analysis.test;

import com.zqykj.AnalysisServiceApplication;
import com.zqykj.analysis.controller.TestController;
import com.zqykj.app.service.dao.ElasticTestDao;
import com.zqykj.app.service.repository.TestEsRepository;
import com.zqykj.domain.bank.StandardBankTransactionFlow;
import com.zqykj.infrastructure.util.ApplicationUtils;
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
        System.out.println(ApplicationUtils.getBean(TestController.class));
    }
}
