/**
 * @作者 Mcj
 */
package com.zqykj.dao;

import com.zqykj.annotations.Query;
import com.zqykj.domain.aggregate.TeacherInfo;
import com.zqykj.domain.page.Page;
import com.zqykj.domain.page.Pageable;
import com.zqykj.tldw.aggregate.searching.ElasticsearchTemplateOperations;

/**
 * <h1> 聚合查询测试类 </h1>
 */
public interface TeacherInfoDao extends ElasticsearchTemplateOperations<TeacherInfo, String> {

    /**
     * <h2> 按照分页查询参数查询全部数据 </h2>
     */
    @Query("{\"match_all\" : {}}")
    Page<TeacherInfo> matchAll(Pageable pageable);
}
