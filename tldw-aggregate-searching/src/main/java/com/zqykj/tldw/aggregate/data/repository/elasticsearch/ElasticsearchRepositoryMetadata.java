/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.data.repository.elasticsearch;

import com.zqykj.infrastructure.util.ClassTypeInformation;
import com.zqykj.infrastructure.util.TypeInformation;
import com.zqykj.tldw.aggregate.data.repository.AbstractRepositoryMetadata;
import com.zqykj.tldw.aggregate.searching.BaseOperations;
import org.springframework.util.Assert;

import java.util.List;
import java.util.function.Supplier;

public class ElasticsearchRepositoryMetadata extends AbstractRepositoryMetadata {

    private static final String MUST_BE_A_REPOSITORY = String.format("Given type must be assignable to %s!",
            BaseOperations.class);

    private final Class<?> idType;
    private final Class<?> domainType;

    /**
     * Creates a new {@link ElasticsearchRepositoryMetadata} for the given repository interface.
     *
     * @param repositoryInterface must not be {@literal null}.
     */
    public ElasticsearchRepositoryMetadata(Class<?> repositoryInterface) {

        super(repositoryInterface);
        Assert.isTrue(BaseOperations.class.isAssignableFrom(repositoryInterface), MUST_BE_A_REPOSITORY);

        List<TypeInformation<?>> arguments = ClassTypeInformation.from(repositoryInterface) //
                .getRequiredSuperTypeInformation(BaseOperations.class)//
                .getTypeArguments();

        this.domainType = resolveTypeParameter(arguments, 0,
                () -> String.format("Could not resolve domain type of %s!", repositoryInterface));
        this.idType = resolveTypeParameter(arguments, 1,
                () -> String.format("Could not resolve id type of %s!", repositoryInterface));
    }

    private static Class<?> resolveTypeParameter(List<TypeInformation<?>> arguments, int index,
                                                 Supplier<String> exceptionMessage) {

        if (arguments.size() <= index || arguments.get(index) == null) {
            throw new IllegalArgumentException(exceptionMessage.get());
        }

        return arguments.get(index).getType();
    }

    public Class<?> getIdType() {
        return this.idType;
    }

    public Class<?> getDomainType() {
        return this.domainType;
    }
}
