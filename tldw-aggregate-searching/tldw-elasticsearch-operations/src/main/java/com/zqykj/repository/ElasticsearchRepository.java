/**
 * @作者 Mcj
 */
package com.zqykj.repository;


import com.zqykj.annotations.NoRepositoryBean;

/**
 * <h1> Elasticsearch basic function components </h1>
 *
 * @param <T>  the domain type the operations manages
 * @param <ID> the type of the id of the entity the operations manages
 * @author zhangkehou
 * @author machengjun
 */
@NoRepositoryBean
public interface ElasticsearchRepository<T, ID> extends CrudRepository {

}
