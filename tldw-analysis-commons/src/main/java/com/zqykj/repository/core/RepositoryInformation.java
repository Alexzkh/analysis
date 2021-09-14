/**
 * @作者 Mcj
 */
package com.zqykj.repository.core;


import com.zqykj.util.Streamable;

import java.lang.reflect.Method;

/**
 * <h1> Additional repository specific information </h1>
 */
public interface RepositoryInformation extends RepositoryMetadata {

    /**
     * <h2> 返回用于创建代理支持实例的基类 (Repository interface 对应的 实现类)  </h2>
     */
    Class<?> getRepositoryBaseClass();

    /**
     * <h2> Returns all methods considered to be query methods. </h2>
     */
    Streamable<Method> getQueryMethods();
}
