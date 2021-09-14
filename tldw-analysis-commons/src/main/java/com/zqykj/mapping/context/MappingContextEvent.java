/**
 * @作者 Mcj
 */
package com.zqykj.mapping.context;

import com.zqykj.mapping.PersistentEntity;
import com.zqykj.mapping.PersistentProperty;
import org.springframework.context.ApplicationEvent;
import org.springframework.util.Assert;

/**
 * <h1> 用来发布Event的包装类 </h1>
 */
public class MappingContextEvent<E extends PersistentEntity<?, P>, P extends PersistentProperty<P>> extends
        ApplicationEvent {

    private static final long serialVersionUID = 1336466833846092490L;

    private final AbstractMappingContext<?, ?> source;
    private final E entity;

    public MappingContextEvent(AbstractMappingContext<?, ?> source, E entity) {
        super(source);

        Assert.notNull(source, "Source MappingContext must not be null!");
        Assert.notNull(entity, "Entity must not be null!");

        this.source = source;
        this.entity = entity;
    }

    public E getPersistentEntity() {
        return entity;
    }


    public boolean wasEmittedBy(AbstractMappingContext<?, ?> context) {
        return this.source.equals(context);
    }
}
