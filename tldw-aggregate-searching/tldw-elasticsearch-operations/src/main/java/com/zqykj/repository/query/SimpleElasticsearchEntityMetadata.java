/**
 * @作者 Mcj
 */
package com.zqykj.repository.query;

import com.zqykj.core.mapping.ElasticsearchPersistentEntity;
import org.springframework.util.Assert;

/**
 * <h1> SimpleElasticsearchEntityMetadata </h1>
 */
public class SimpleElasticsearchEntityMetadata<T> implements ElasticsearchEntityMetadata<T> {

    private final Class<T> type;
    private final ElasticsearchPersistentEntity<?> entity;

    public SimpleElasticsearchEntityMetadata(Class<T> type, ElasticsearchPersistentEntity<?> entity) {

        Assert.notNull(type, "Type must not be null!");
        Assert.notNull(entity, "Entity must not be null!");

        this.type = type;
        this.entity = entity;
    }

    @Override
    public String getIndexName() {
        return entity.getIndexName();
    }

    @Override
    public Class<T> getJavaType() {
        return type;
    }
}
