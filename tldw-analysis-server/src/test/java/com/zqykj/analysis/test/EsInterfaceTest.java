/**
 * @作者 Mcj
 */
package com.zqykj.analysis.test;

import com.alibaba.fastjson.JSON;
import com.zqykj.app.service.dao.ElasticTestDao;
import com.zqykj.dao.TeacherInfoDao;
import com.zqykj.domain.aggregate.TeacherInfo;
import com.zqykj.domain.bank.StandardBankTransactionFlow;
import com.zqykj.domain.page.Page;
import com.zqykj.domain.page.PageRequest;
import com.zqykj.domain.page.Sort;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class EsInterfaceTest {

    @Autowired
    private ElasticTestDao testDao;

    @Autowired
    private TeacherInfoDao teacherInfoDao;

    @Test
    public void testEsInterface() throws Exception {
        System.out.println(testDao.search(new MatchAllQueryBuilder(), StandardBankTransactionFlow.class).size());
        List<StandardBankTransactionFlow> cash_mark = testDao.existsByField("cash_mark");
    }

    @Test
    public void printTeacherInfoDao() {
        System.out.println(teacherInfoDao);
    }

    /**
     * 聚合查询案列测试
     */

    @Test
    public void testPageSearch() {
        Page<TeacherInfo> infoPage = teacherInfoDao.matchAll(PageRequest.of(0, 2,
                Sort.by(new Sort.Order(Sort.Direction.DESC, "age"))));
        System.out.println(infoPage.getAggregations());
        System.out.println(JSON.toJSONString(infoPage.getContent()));
        System.out.println(infoPage.getMaxScore());
        System.out.println(JSON.toJSONString(infoPage.getPageable()));
        System.out.println(infoPage.getTotalElements());
        System.out.println(infoPage.getTotalPages());
        System.out.println(JSON.toJSONString(infoPage.getSort()));
    }
}
