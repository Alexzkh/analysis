/**
 * @作者 Mcj
 */
package com.zqykj.app.service.dao;


import com.zqykj.annotations.Query;
import com.zqykj.domain.aggregate.TeacherInfo;
import com.zqykj.domain.page.Page;
import com.zqykj.domain.page.Pageable;
import com.zqykj.domain.routing.Routing;
import com.zqykj.tldw.aggregate.searching.esclientrhl.ElasticsearchOperations;

import java.util.List;
import java.util.Optional;

/**
 * <h1> 聚合查询测试类 </h1>
 */
public interface TeacherInfoDao extends ElasticsearchOperations<TeacherInfo, String> {

    /**
     * <h2> 按照分页查询参数查询全部数据 </h2>
     */
    @Query("{\"match_all\" : {}}")
    Page<TeacherInfo> matchAll(Pageable pageable);

    @Query("{\"aggs\" : { \"salary_status\" : { \"stats\" : {\"field\" : \"salary\"}}}}")
    Page<TeacherInfo> testAggregate(Pageable pageable, Routing routing);

    @Query("{\"match_all\" : {}}")
    Optional<List<TeacherInfo>> matchAllofOptional();

    @Query("{\"match_all\" : {}}")
    Page<TeacherInfo> matchAllofList(Pageable pageable);
}
