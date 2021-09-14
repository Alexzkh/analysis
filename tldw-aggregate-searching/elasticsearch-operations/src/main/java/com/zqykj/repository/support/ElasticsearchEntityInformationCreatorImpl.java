/**
 * @作者 Mcj
 */
package com.zqykj.repository.support;

import com.zqykj.core.mapping.ElasticsearchPersistentEntity;
import com.zqykj.core.mapping.ElasticsearchPersistentProperty;
import com.zqykj.mapping.context.MappingContext;
import org.springframework.util.Assert;

/**
 * <h1> ElasticsearchEntityInformationCreatorImpl </h1>
 */
public class ElasticsearchEntityInformationCreatorImpl implements ElasticsearchEntityInformationCreator {

    private final MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext;

    public ElasticsearchEntityInformationCreatorImpl(
            MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext) {

        Assert.notNull(mappingContext, "MappingContext must not be null!");

        this.mappingContext = mappingContext;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, ID> ElasticsearchEntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {

        ElasticsearchPersistentEntity<T> persistentEntity = (ElasticsearchPersistentEntity<T>) mappingContext
                .getRequiredPersistentEntity(domainClass);

        Assert.notNull(persistentEntity, String.format("Unable to obtain mapping metadata for %s!", domainClass));
        Assert.notNull(persistentEntity.getIdProperty(), String.format("No id property found for %s!", domainClass));

        return new MappingElasticsearchEntityInformation<>(persistentEntity);
    }
}
