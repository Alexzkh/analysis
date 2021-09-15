/**
 * @作者 Mcj
 */
package com.zqykj.mapping.context;

import com.zqykj.mapping.MappingException;
import com.zqykj.mapping.PersistentEntity;
import com.zqykj.mapping.PersistentProperty;
import com.zqykj.util.TypeInformation;
import org.springframework.lang.Nullable;

import java.util.Collection;

/**
 * <h1> 该接口定义了整体上下文, 包括所有已知的 PersistentEntity 实例和方法以获取 </h1>
 */
public interface MappingContext<E extends PersistentEntity<?, P>, P extends PersistentProperty<P>> {

    /**
     * <h2> 获取当前映射上下文所有的索引类描述 PersistentEntity </h2>
     */
    Collection<E> getPersistentEntities();

    /**
     * <h2> 根据指定Class type 获取对应的  PersistentEntity</h2>
     */
    @Nullable
    E getPersistentEntity(Class<?> type);

    /**
     * <h2> 根据指定Class type 获取对应的  PersistentEntity</h2>
     *
     * @return PersistentEntity 如果为null,抛出 {@link MappingException}
     */
    default E getRequiredPersistentEntity(Class<?> type) throws MappingException {

        E entity = getPersistentEntity(type);

        if (entity != null) {
            return entity;
        }

        throw new MappingException(String.format("Couldn't find PersistentEntity for type %s!", type));
    }

    /**
     * <h2> 判断是否存在指定类型的 PersistentEntity </h2>
     */
    boolean hasPersistentEntityFor(Class<?> type);

    /**
     * <h2> 根据TypeInformation 获取对应的 PersistentEntity </h2>
     *
     * @return PersistentEntity 如果为null,抛出 {@link MappingException}
     */
    @Nullable
    E getPersistentEntity(TypeInformation<?> type);

    /**
     * <h2> 根据TypeInformation 获取对应的 PersistentEntity </h2>
     */
    default E getRequiredPersistentEntity(TypeInformation<?> type) throws MappingException {

        E entity = getPersistentEntity(type);

        if (entity != null) {
            return entity;
        }

        throw new MappingException(String.format("Couldn't find PersistentEntity for type %s!", type));
    }

    /**
     * <h2> 根据 PersistentProperty 获取对应的PersistentEntity </h2>
     */
    @Nullable
    E getPersistentEntity(P persistentProperty);

    /**
     * <h2> 根据 PersistentProperty 获取对应的PersistentEntity </h2>
     *
     * @return PersistentEntity 如果为null,抛出 {@link MappingException}
     */
    default E getRequiredPersistentEntity(P persistentProperty) throws MappingException {

        E entity = getPersistentEntity(persistentProperty);

        if (entity != null) {
            return entity;
        }

        throw new MappingException(String.format("Couldn't find PersistentEntity for property %s!", persistentProperty));
    }

    Collection<TypeInformation<?>> getManagedTypes();
}
