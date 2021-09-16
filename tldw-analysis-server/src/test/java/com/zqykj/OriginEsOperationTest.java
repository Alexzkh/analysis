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
import com.zqykj.util.JacksonUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

@SpringBootTest
@Slf4j
public class OriginEsOperationTest {

    @Autowired
    private TeacherInfoDao teacherInfoDao;

//    @Autowired
//    private EntranceRepository repository;


//    @Test
//    public void testTeacherDao() {
//        Page<TeacherInfo> teacherInfos = teacherInfoDao.matchAll(PageRequest.of(0, 2));
//        System.out.println(teacherInfos);
//    }

    @Test
    public void testTeacherById() throws Exception {

        Optional<TeacherInfo> teacherInfo = teacherInfoDao.findById("110", "22", TeacherInfo.class);
        log.info(JacksonUtils.toJson(teacherInfo.orElse(null)));
    }

    @Test
    public void testQueryAnn() throws Exception {

        Page<TeacherInfo> teacherInfo = teacherInfoDao.matchAll(new PageRequest(0, 20, Sort.unsorted()),
                new EntityClass(TeacherInfo.class));
        log.info(JacksonUtils.toJson(teacherInfo.getContent()));
    }
}
