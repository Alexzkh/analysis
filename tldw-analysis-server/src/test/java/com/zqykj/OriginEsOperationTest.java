/**
 * @作者 Mcj
 */
package com.zqykj;


import com.zqykj.app.service.dao.TeacherInfoDao;
import com.zqykj.domain.EntityClass;
import com.zqykj.domain.Page;
import com.zqykj.domain.PageRequest;
import com.zqykj.domain.Sort;
import com.zqykj.domain.aggregate.TeacherInfo;
import com.zqykj.repository.EntranceRepository;
import com.zqykj.util.JacksonUtils;
import com.zqykj.util.WebApplicationContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@Slf4j
public class OriginEsOperationTest {

    @Autowired
    private ApplicationContext applicationContext;


    private EntranceRepository entranceRepository = WebApplicationContext.getBean(EntranceRepository.class);


    @Test
    public void testTeacherById() throws Exception {

        EntranceRepository entranceRepository = applicationContext.getBean(EntranceRepository.class);
        Optional<TeacherInfo> teacherInfo = entranceRepository.findById("110", "22", TeacherInfo.class);
        log.info(JacksonUtils.toJson(teacherInfo.orElse(null)));
    }

    @Test
    public void testQueryAnn() {

        TeacherInfoDao teacherInfoDao = applicationContext.getBean(TeacherInfoDao.class);
        Page<TeacherInfo> teacherInfo = teacherInfoDao.matchAll(new PageRequest(0, 20, Sort.unsorted()),
                new EntityClass(TeacherInfo.class));
        log.info(JacksonUtils.toJson(teacherInfo.getContent()));
    }

    @Test
    public void testSave() {
        TeacherInfo teacherInfo = new TeacherInfo();
        teacherInfo.setAge(1);
        teacherInfo.setId("1");
        teacherInfo.setJob("test job");
        teacherInfo.setName("test name");
        teacherInfo.setSalary(new BigDecimal("1.00"));
        teacherInfo.setSex(1);
        entranceRepository.save(teacherInfo, "82c3e52e-019b-4d02-a4a3-e4fecc7f347b", TeacherInfo.class);
    }

    @Test
    public void testSaveAll() {
        List<TeacherInfo> teacherInfos = new ArrayList<>();
        for (int i = 2; i < 30; i++) {
            TeacherInfo teacherInfo = new TeacherInfo();
            teacherInfo.setAge(i);
            teacherInfo.setId("1" + i);
            teacherInfo.setJob("test job " + i);
            teacherInfo.setName("test name " + i);
            teacherInfo.setSalary(new BigDecimal("1.00"));
            teacherInfo.setSex(i);
            teacherInfos.add(teacherInfo);
        }
        entranceRepository.saveAll(teacherInfos, "61e9e22a-a6b1-4838-8cea-df8995bc2d8c", TeacherInfo.class);
    }

}
