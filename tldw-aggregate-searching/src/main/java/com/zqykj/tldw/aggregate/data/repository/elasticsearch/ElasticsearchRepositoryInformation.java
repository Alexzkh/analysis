/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.data.repository.elasticsearch;

import com.zqykj.infrastructure.util.TypeInformation;
import com.zqykj.tldw.aggregate.data.repository.RepositoryInformation;
import com.zqykj.tldw.aggregate.data.repository.RepositoryMetadata;
import com.zqykj.tldw.aggregate.BaseOperations;
import org.springframework.util.Assert;

import java.lang.reflect.Method;


/**
 * <h1> Elasticsearch Repository Information </h1>
 */
public class ElasticsearchRepositoryInformation extends RepositoryInformation {

    private final RepositoryMetadata metadata;
    private final Class<?> repositoryBaseClass;

    /**
     * Creates a new {@link ElasticsearchRepositoryInformation} for the given repository interface and repository base class.
     *
     * @param metadata            must not be {@literal null}.
     * @param repositoryBaseClass must not be {@literal null}.
     */
    public ElasticsearchRepositoryInformation(RepositoryMetadata metadata, Class<?> repositoryBaseClass) {
        super(metadata.getRepositoryInterface());
        Assert.notNull(metadata, "Repository metadata must not be null!");
        Assert.notNull(repositoryBaseClass, "Repository base class must not be null!");
        this.metadata = metadata;
        this.repositoryBaseClass = repositoryBaseClass;
    }

    @Override
    public Class<?> getDomainType() {
        return metadata.getDomainType();
    }

    @Override
    public Class<?> getIdType() {
        return metadata.getIdType();
    }

    @Override
    public Class<?> getRepositoryInterface() {
        return metadata.getRepositoryInterface();
    }

    @Override
    public Class<?> getReturnedDomainClass(Method method) {
        return metadata.getReturnedDomainClass(method);
    }

    @Override
    public TypeInformation<?> getReturnType(Method method) {
        return metadata.getReturnType(method);
    }

    public static boolean isGenericRepositoryInterface(Class<?> interfaze) {

        return BaseOperations.class.equals(interfaze);
    }

    public Class<?> getRepositoryBaseClass() {
        return this.repositoryBaseClass;
    }
}
