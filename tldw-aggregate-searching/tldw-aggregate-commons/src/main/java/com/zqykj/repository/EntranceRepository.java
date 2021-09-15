/**
 * @作者 Mcj
 */
package com.zqykj.repository;

import com.zqykj.annotations.NoRepositoryBean;

/**
 *  <h1> 提供给外部使用的公共入口Repository </h1>
 * */
@NoRepositoryBean
public interface EntranceRepository<T,ID> extends CrudRepository<T,ID>{


}
