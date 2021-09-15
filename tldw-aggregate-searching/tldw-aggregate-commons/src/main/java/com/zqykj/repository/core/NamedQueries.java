/**
 * @作者 Mcj
 */
package com.zqykj.repository.core;

/**
 * <h1> 命名查询 </h1>
 */
public interface NamedQueries {

    /**
     * <h2> 是否存在该查询名称  </h2>
     *
     * @param queryName must not be {@literal null} or empty.
     * @throws IllegalArgumentException in case the given name is {@literal null} or empty.
     */
    boolean hasQuery(String queryName);

    /**
     * <h2> 返回具有给定名称的命名查询 </h2>
     *
     * @param queryName must not be {@literal null} or empty.
     * @throws IllegalArgumentException in case no query with the given name exists.
     */
    String getQuery(String queryName);
}
