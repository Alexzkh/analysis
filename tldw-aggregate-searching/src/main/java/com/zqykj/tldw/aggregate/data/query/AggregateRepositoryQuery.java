/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.data.query;

import org.springframework.lang.Nullable;

import java.lang.reflect.Method;

public interface AggregateRepositoryQuery {

    /**
     * <h2> 根据Aggregate Repository interface method handle query  </h2>
     *
     * @param parameters must not be {@literal null}.
     * @return execution result. Can be {@literal null}.
     */
    @Nullable
    Object execute(Object[] parameters);

    /**
     * <h2> 返回指定关联的method </h2>
     */
    Method getQueryMethod();
}
