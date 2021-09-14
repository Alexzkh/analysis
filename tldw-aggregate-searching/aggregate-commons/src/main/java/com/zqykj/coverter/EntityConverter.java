/**
 * @作者 Mcj
 */
package com.zqykj.coverter;

import com.zqykj.mapping.PersistentEntity;
import com.zqykj.mapping.PersistentProperty;
import com.zqykj.mapping.context.MappingContext;
import org.springframework.core.convert.ConversionService;

/**
 * <h1>
 * 结合 {@link EntityReader} 和 {@link EntityWriter} , 并且使用 {@link MappingContext}
 * 处理 E (给定实体类) 上的 P (property) 转换
 * </h1>
 */
public interface EntityConverter<E extends PersistentEntity<?, P>, P extends PersistentProperty<P>, T, S>
        extends EntityReader<T, S>, EntityWriter<T, S> {

    /**
     * <h2> 返回转换器使用的底层 {@link MappingContext} </h2>
     *
     * @return never {@literal null}
     */
    MappingContext<? extends E, P> getMappingContext();

    /**
     * <h2> 返回一个转换器  {@link ConversionService} </h2>
     *
     * @return never {@literal null}.
     */
    ConversionService getConversionService();
}
