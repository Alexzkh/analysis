/**
 * @作者 Mcj
 */
package com.zqykj.repository.core.support;

import com.zqykj.repository.Repository;
import com.zqykj.repository.core.RepositoryMetadata;
import com.zqykj.util.ClassTypeInformation;
import com.zqykj.util.TypeInformation;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.function.Supplier;

/**
 * <h1> Default implementation of {@link RepositoryMetadata}. Will inspect generic types of {@link Repository} to find out
 * * about domain and id class. </h1>
 */
public class DefaultRepositoryMetadata extends AbstractRepositoryMetadata {

    // 给定的repository interface type 必须可指派给 Repository.class 类型
    private static final String MUST_BE_A_REPOSITORY = String.format("Given type must be assignable to %s!",
            Repository.class);

    private final Class<?> idType;
    private final Class<?> domainType;

    /**
     * Creates a new {@link DefaultRepositoryMetadata} for the given repository interface.
     *
     * @param repositoryInterface must not be {@literal null}.
     */
    public DefaultRepositoryMetadata(Class<?> repositoryInterface) {

        super(repositoryInterface);
        Assert.isTrue(Repository.class.isAssignableFrom(repositoryInterface), MUST_BE_A_REPOSITORY);

        List<TypeInformation<?>> arguments = ClassTypeInformation.from(repositoryInterface) //
                .getRequiredSuperTypeInformation(Repository.class)//
                .getTypeArguments();
        // 可能这个repositoryInterface 这个类没有泛型参数, 只有泛型方法
        if (CollectionUtils.isEmpty(arguments)) {
            this.domainType = null;
            this.idType = null;
        } else {
            this.domainType = resolveTypeParameter(arguments, 0,
                    () -> String.format("Could not resolve domain type of %s!", repositoryInterface));
            this.idType = resolveTypeParameter(arguments, 1,
                    () -> String.format("Could not resolve id type of %s!", repositoryInterface));
        }
    }

    /**
     * <h2> 获取Repository 泛型的具体参数的Class </h2>
     */
    private static Class<?> resolveTypeParameter(List<TypeInformation<?>> arguments, int index,
                                                 Supplier<String> exceptionMessage) {

        if (arguments.size() <= index || arguments.get(index) == null) {
            throw new IllegalArgumentException(exceptionMessage.get());
        }

        return arguments.get(index).getType();
    }

    /**
     * <h2> Repository interface Id  </h2>
     */
    public Class<?> getIdType() {
        return this.idType;
    }

    /**
     * <h2> Repository interface Index Class  </h2>
     */
    public Class<?> getDomainType() {
        return this.domainType;
    }
}
