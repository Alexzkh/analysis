/**
 * @作者 Mcj
 */
package com.zqykj.repository;

import org.springframework.context.annotation.Primary;

/**
 * <h1> 提供给外部使用的公共入口Repository </h1>
 */
@Primary
public interface EntranceRepository extends CrudRepository {

}
