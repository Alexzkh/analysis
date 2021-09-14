/**
 * @作者 Mcj
 */
package com.zqykj.repository.core;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * <h1> Extension of {@link EntityMetadata} to add functionality to query information of entity instances. </h1>
 */
public interface EntityInformation<T, ID> extends EntityMetadata<T> {

    /**
     * <h2> 返回给定实体的ID </h2>
     *
     * @param entity must never be {@literal null}
     */
    @Nullable
    ID getId(T entity);

    /**
     * <h2> 返回给定实体的ID,如果为空,抛出异常 </h2>
     *
     * @param entity must not be {@literal null}.
     * @return the identifier of the given entity
     * @throws IllegalArgumentException in case no id could be obtained from the given entity
     */
    default ID getRequiredId(T entity) throws IllegalArgumentException {

        Assert.notNull(entity, "Entity must not be null!");

        ID id = getId(entity);

        if (id != null) {
            return id;
        }

        throw new IllegalArgumentException(String.format("Could not obtain required identifier from entity %s!", entity));
    }

    /**
     * <h2> 返回给定实体的ID  Class </h2>
     */
    Class<ID> getIdType();
}
