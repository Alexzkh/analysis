/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.data.repository.elasticsearch;

import com.zqykj.tldw.aggregate.index.elasticsearch.SimpleElasticSearchPersistentEntity;
import org.elasticsearch.index.VersionType;

/**
 * <h1> 描述 domain的entity information </h1>
 */
public class ElasticsearchEntityInformation<T, M> {

    private final SimpleElasticSearchPersistentEntity<T> persistentEntity;

    public ElasticsearchEntityInformation(SimpleElasticSearchPersistentEntity<T> persistentEntity) {
        this.persistentEntity = persistentEntity;
    }

    public SimpleElasticSearchPersistentEntity<T> getPersistentEntity() {
        return persistentEntity;
    }

    public String getIdAttribute() {
        return persistentEntity.getRequiredIdProperty().getFieldName();
    }

    public String getIndexName() {
        return persistentEntity.getIndexName();
    }

//    public Long getVersion(T entity) {
//
//        SimpleElasticSearchPersistentEntity<T> versionProperty = persistentEntity.getVersionProperty();
//        try {
//            return versionProperty != null ? (Long) persistentEntity.getPropertyAccessor(entity).getProperty(versionProperty)
//                    : null;
//        } catch (Exception e) {
//            throw new IllegalStateException("failed to load version field", e);
//        }
//    }

    public VersionType getVersionType() {
        return persistentEntity.getVersionType();
    }
}
