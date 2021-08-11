/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.index.context;

import com.zqykj.tldw.aggregate.index.mapping.PersistentEntity;
import com.zqykj.tldw.aggregate.index.mapping.PersistentProperty;
import org.springframework.context.ApplicationEvent;
import org.springframework.util.Assert;

/**
 * @author Mcj
 *
 * <h2> 用来发布Event的包装类 </h2>
 */
public class AggregateDataSourceMappingContextEvent<E extends PersistentEntity<?, P>, P extends PersistentProperty<P>> extends
        ApplicationEvent {

    private static final long serialVersionUID = 1336466833846092490L;

    private final AbstractMappingContext<?, ?> source;
    private final E entity;

    public AggregateDataSourceMappingContextEvent(AbstractMappingContext<?, ?> source, E entity) {
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
