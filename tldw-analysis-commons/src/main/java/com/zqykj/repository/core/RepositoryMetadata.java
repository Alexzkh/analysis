/**
 * @作者 Mcj
 */
package com.zqykj.repository.core;

import com.zqykj.util.TypeInformation;

import java.lang.reflect.Method;

/**
 * <h1> repository 元数据 </h1>
 */
public interface RepositoryMetadata {

    /**
     * Returns the id class the given class is declared for.
     *
     * @return the id class of the entity managed by the repository.
     */
    Class<?> getIdType();

    /**
     * Returns the domain class the repository is declared for.
     *
     * @return the domain class the repository is handling.
     */
    Class<?> getDomainType();

    /**
     * Returns the repository interface.
     */
    Class<?> getRepositoryInterface();

    /**
     * Returns the type {@link Method} return type as it is declared in the repository. Considers suspended methods and
     * does not unwrap component types but leaves those for further inspection.
     */
    TypeInformation<?> getReturnType(Method method);

    /**
     * Returns the domain class returned by the given {@link Method}
     *
     * @see #getReturnType(Method)
     */
    Class<?> getReturnedDomainClass(Method method);
}
