/**
 * @作者 Mcj
 */
package com.zqykj;

import com.alibaba.fastjson.JSON;
import com.zqykj.app.service.dao.TeacherInfoDao;
import com.zqykj.domain.aggregate.TeacherInfo;
import com.zqykj.domain.page.Page;
import com.zqykj.domain.page.PageRequest;
import com.zqykj.domain.page.Sort;
import com.zqykj.domain.routing.Routing;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

@SpringBootTest
@RunWith(SpringRunner.class)
public class AggregateDataTest {

    @Autowired
    private TeacherInfoDao teacherInfoDao;

    @Test
    public void test() {
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

    @Test
    public void testSalaryAggregations() {
        Page<TeacherInfo> teacherInfos = teacherInfoDao.testAggregate(PageRequest.of(0, 2,
                Sort.by(new Sort.Order(Sort.Direction.DESC, "age"))), new Routing("1231231"));
        System.out.println(JSON.toJSONString(teacherInfos.getAggregations()));
    }

    @Test
    public void testOptionalQuery() {
        Optional<List<TeacherInfo>> teacherInfos = teacherInfoDao.matchAllofOptional();
        System.out.println(JSON.toJSONString(teacherInfos));
    }

    @Test
    public void testListQuery() {
        Page<TeacherInfo> teacherInfos = teacherInfoDao.matchAllofList(PageRequest.of(0, 3));
        System.out.println(JSON.toJSONString(teacherInfos.getContent()));
    }
}
