/**
 * @作者 Mcj
 */
package com.zqykj.app.service.dao;

import com.zqykj.annotations.Query;
import com.zqykj.domain.aggregate.AliasPojo;
import com.zqykj.domain.routing.Routing;
import com.zqykj.tldw.aggregate.searching.esclientrhl.ElasticsearchOperations;

import java.util.Optional;

public interface AliasPojoDao extends ElasticsearchOperations<AliasPojo, String> {


    @Query("{\"term\" : {\"?0\" : {\"value\" : \"?1\"}}}")
    Optional<AliasPojo> findIdByRouting(String field, String value, Routing routing);
}
