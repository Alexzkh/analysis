/**
 * @作者 Mcj
 */
package com.zqykj.mapping;

import org.springframework.lang.Nullable;

/**
 * <h1> PersistentProperty 访问器 </h1>
 */
public interface PersistentPropertyAccessor<T> {

    /**
     * <h2> 返回 Bean 实例 给定 PersistentProperty 的 值 </h2>
     *
     * @param property must not be {@literal null}.
     * @return can be {@literal null}.
     */
    @Nullable
    Object getProperty(PersistentProperty<?> property);

    /**
     * <h2> Returns the underlying bean. The actual instance may change between </h2>
     *
     * @return will never be {@literal null}.
     */
    T getBean();

    <S> S getProperty(PersistentProperty<?> property, Class<S> targetType);

}
