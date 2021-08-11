/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.index.elasticsearch;


import com.zqykj.infrastructure.util.TypeInformation;
import com.zqykj.tldw.aggregate.index.context.AbstractMappingContext;
import com.zqykj.tldw.aggregate.index.model.Property;
import com.zqykj.tldw.aggregate.index.model.SimpleTypeHolder;

/**
 * <h1> 描述索引类的entity 与其 property 关系(扫描指定注解的持久化实体) </h1>
 */
public class SimpleElasticsearchMappingContext
        extends AbstractMappingContext<SimpleElasticSearchPersistentEntity<?>, SimpleElasticSearchPersistentProperty> {

    private boolean autoIndexCreation = false;

    @Override
    protected <T> SimpleElasticSearchPersistentEntity<?> createPersistentEntity(TypeInformation<T> typeInformation) {
        return new SimpleElasticSearchPersistentEntity<>(typeInformation);
    }

    @Override
    protected SimpleElasticSearchPersistentProperty createPersistentProperty(Property property, SimpleElasticSearchPersistentEntity<?> owner, SimpleTypeHolder simpleTypeHolder) {
        return new SimpleElasticSearchPersistentProperty(property, owner, simpleTypeHolder);
    }

    public boolean isAutoIndexCreation() {
        return autoIndexCreation;
    }

    public void setAutoIndexCreation(boolean autoCreateIndexes) {
        this.autoIndexCreation = autoCreateIndexes;
    }
}
