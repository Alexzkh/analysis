/**
 * @作者 Mcj
 */
package com.zqykj.repository.query;

/**
 *   <h1> 实现类默认方法查询 (直接代理到默认实现类) </h1>
 * */
public class DefaultRepositoryQuery implements RepositoryQuery {


    @Override
    public Object execute(Object[] parameters) {
        return parameters;
    }

    @Override
    public QueryMethod getQueryMethod() {
        return null;
    }
}
