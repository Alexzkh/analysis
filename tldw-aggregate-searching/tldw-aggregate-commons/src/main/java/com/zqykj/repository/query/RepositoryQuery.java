/**
 * @作者 Mcj
 */
package com.zqykj.repository.query;

import org.springframework.lang.Nullable;

/**
 * <h1> Interface for a query abstraction. </h1>
 */
public interface RepositoryQuery {

    /**
     * Executes the {@link RepositoryQuery} with the given parameters.
     *
     * @param parameters must not be {@literal null}.
     * @return execution result. Can be {@literal null}.
     */
    @Nullable
    Object execute(Object[] parameters);

    /**
     * Returns the related {@link QueryMethod}.
     *
     * @return never {@literal null}.
     */
    QueryMethod getQueryMethod();
}
