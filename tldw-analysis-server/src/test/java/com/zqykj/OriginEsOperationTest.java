/**
 * @作者 Mcj
 */
package com.zqykj;


import com.zqykj.app.service.dao.TeacherInfoDao;
import com.zqykj.domain.Page;
import com.zqykj.domain.PageRequest;
import com.zqykj.domain.aggregate.TeacherInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class OriginEsOperationTest {

    @Autowired
    private TeacherInfoDao teacherInfoDao;


    @Test
    public void testTeacherDao() {
        Page<TeacherInfo> teacherInfos = teacherInfoDao.matchAll(PageRequest.of(0, 2));
        System.out.println(teacherInfos);
    }
}
