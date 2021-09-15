/**
 * @作者 Mcj
 */
package com.zqykj.repository.core;

/**
 * <h1> 实体元数据 </h1>
 */
public interface EntityMetadata<T> {

    /**
     * Returns the actual domain class type.
     *
     * @return
     */
    Class<T> getJavaType();
}
