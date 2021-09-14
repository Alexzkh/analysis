/**
 * @作者 Mcj
 */
package com.zqykj.repository.core.support;

import com.zqykj.repository.query.RepositoryQuery;

/**
 * <h1> 查询创建监听器 {@link RepositoryQuery}  </h1>
 */
public interface QueryCreationListener<T extends RepositoryQuery> {

    /**
     * <h2> 将在创建  {@link RepositoryQuery} 之后调用 </h2>
     *
     * @param query
     */
    void onCreation(T query);
}
