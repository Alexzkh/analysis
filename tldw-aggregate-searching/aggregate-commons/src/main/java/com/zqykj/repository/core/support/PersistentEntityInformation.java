/**
 * @作者 Mcj
 */
package com.zqykj.repository.core.support;

import com.zqykj.mapping.PersistentEntity;
import com.zqykj.mapping.PersistentProperty;
import com.zqykj.repository.core.EntityInformation;
import org.springframework.lang.Nullable;

/**
 * <h1> 持久化实体信息实现 {@link PersistentEntity} </h1>
 */
public class PersistentEntityInformation<T, ID> implements EntityInformation<T, ID> {

    private final PersistentEntity<T, ? extends PersistentProperty<?>> persistentEntity;

    public PersistentEntityInformation(PersistentEntity<T, ? extends PersistentProperty<?>> persistentEntity) {
        this.persistentEntity = persistentEntity;
    }

    /**
     * <h2> 获取给定 instance 的ID值 </h2>
     */
    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public ID getId(T entity) {
        return (ID) persistentEntity.getIdentifierAccessor(entity);
    }

    /**
     * <h2> 获取当前索引类的ID type </h2>
     */
    @Override
    @SuppressWarnings("unchecked")
    public Class<ID> getIdType() {
        return (Class<ID>) persistentEntity.getRequiredIdProperty().getType();
    }

    /**
     * <h2> 获取当前索引类 type </h2>
     */
    @Override
    public Class<T> getJavaType() {
        return persistentEntity.getType();
    }
}
