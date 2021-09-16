/**
 * @作者 Mcj
 */
package com.zqykj.app.service.dao;


import com.zqykj.annotations.Query;
import com.zqykj.domain.EntityClass;
import com.zqykj.domain.Page;
import com.zqykj.domain.Pageable;
import com.zqykj.domain.aggregate.TeacherInfo;
import com.zqykj.repository.ElasticsearchRepository;
import com.zqykj.repository.EntranceRepository;

import java.util.List;
import java.util.Optional;

/**
 * <h1> 聚合查询测试类 </h1>
 */
public interface TeacherInfoDao extends EntranceRepository {

    /**
     * <h2> 按照分页查询参数查询全部数据 </h2>
     */
    @Query("{\"match_all\" : {}}")
    Page<TeacherInfo> matchAll(Pageable pageable, EntityClass domain);

    @Query("{\"match_all\" : {}}")
    Optional<List<TeacherInfo>> matchAllofOptional();

    @Query("{\"match_all\" : {}}")
    List<TeacherInfo> matchAllofList();
}
