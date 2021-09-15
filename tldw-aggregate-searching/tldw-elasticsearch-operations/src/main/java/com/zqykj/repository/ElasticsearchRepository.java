/**
 * @作者 Mcj
 */
package com.zqykj.repository;

import com.zqykj.annotations.NoRepositoryBean;

/**
 * <h1> Elasticsearch basic function components </h1>
 *
 * @author zhangkehou
 * @author machengjun
 */
@NoRepositoryBean
public interface ElasticsearchRepository<T, ID> extends CrudRepository<T, ID> {

}
