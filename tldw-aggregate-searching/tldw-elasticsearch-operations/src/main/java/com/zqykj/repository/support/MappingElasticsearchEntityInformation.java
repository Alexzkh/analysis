/**
 * @作者 Mcj
 */
package com.zqykj.repository.support;

import com.zqykj.core.mapping.ElasticsearchPersistentEntity;
import com.zqykj.core.mapping.ElasticsearchPersistentProperty;
import com.zqykj.repository.core.support.PersistentEntityInformation;
import org.elasticsearch.index.VersionType;

/**
 * <h1> Elasticsearch entity information specific implementation</h1>
 */
public class MappingElasticsearchEntityInformation<T, ID> extends PersistentEntityInformation<T, ID>
        implements ElasticsearchEntityInformation<T, ID> {


    private final ElasticsearchPersistentEntity<T> persistentEntity;

    public MappingElasticsearchEntityInformation(ElasticsearchPersistentEntity<T> persistentEntity) {
        super(persistentEntity);
        this.persistentEntity = persistentEntity;
    }

    @Override
    public String getIdAttribute() {
        return persistentEntity.getRequiredIdProperty().getFieldName();
    }

    @Override
    public String getIndex() {
        return persistentEntity.getIndexName();
    }

    @Override
    public Long getVersion(T entity) {

        ElasticsearchPersistentProperty versionProperty = persistentEntity.getVersionProperty();
        try {
            return versionProperty != null ? (Long) persistentEntity.getPropertyAccessor(entity).getProperty(versionProperty)
                    : null;
        } catch (Exception e) {
            throw new IllegalStateException("failed to load version field", e);
        }
    }

    @Override
    public VersionType getVersionType() {
        return persistentEntity.getVersionType();
    }
}
