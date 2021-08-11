/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.index.mongodb;


import com.zqykj.infrastructure.util.TypeInformation;
import com.zqykj.tldw.aggregate.index.model.Property;
import com.zqykj.tldw.aggregate.index.model.SimpleTypeHolder;
import com.zqykj.tldw.aggregate.index.context.AbstractMappingContext;

/**
 * <h1> 描述索引类的entity 与其 property 关系(扫描指定注解的持久化实体) </h1>
 */
public class SimpleMongodbMappingContext
        extends AbstractMappingContext<SimpleMongoPersistentEntity<?>, SimpleMongodbPersistentProperty> {

    private boolean autoIndexCreation = false;

    @Override
    protected <T> SimpleMongoPersistentEntity<?> createPersistentEntity(TypeInformation<T> typeInformation) {
        return new SimpleMongoPersistentEntity<>(typeInformation);
    }

    @Override
    protected SimpleMongodbPersistentProperty createPersistentProperty(Property property, SimpleMongoPersistentEntity<?> owner,
                                                                       SimpleTypeHolder simpleTypeHolder) {
        return new SimpleMongodbPersistentProperty(property, owner, simpleTypeHolder);
    }

    public boolean isAutoIndexCreation() {
        return autoIndexCreation;
    }

    public void setAutoIndexCreation(boolean autoCreateIndexes) {
        this.autoIndexCreation = autoCreateIndexes;
    }


}
