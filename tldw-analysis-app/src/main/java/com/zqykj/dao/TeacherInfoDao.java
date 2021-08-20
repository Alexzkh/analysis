/**
 * @作者 Mcj
 */
package com.zqykj.dao;

import com.zqykj.annotations.Query;
import com.zqykj.domain.aggregate.TeacherInfo;
import com.zqykj.domain.page.Page;
import com.zqykj.domain.page.Pageable;
import com.zqykj.tldw.aggregate.searching.esclientrhl.ElasticsearchOperations;

/**
 * <h1> 聚合查询测试类 </h1>
 */
public interface TeacherInfoDao extends ElasticsearchOperations<TeacherInfo, String> {

    /**
     * <h2> 按照分页查询参数查询全部数据 </h2>
     */
    @Query("{\"match_all\" : {}}")
    Page<TeacherInfo> matchAll(Pageable pageable);
<<<<<<< HEAD

    //    @Query("{\"aggs\" : { \"salary_status\" : { \"stats\" : {\"field\" : \"salary\"}}}}")
    @Query("{\"match_all\" : {}}")
    Page<TeacherInfo> testAggregate(Pageable pageable, Route route);

    @Query("{\"match_all\" : {}}")
    Optional<List<TeacherInfo>> matchAllOfOptional();

    @Query("{\"match_all\" : {}}")
    List<TeacherInfo> matchAllofList();
=======
>>>>>>> d3dca9a691c598b285f8af5dbbdffb72c967841d
}
