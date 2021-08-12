/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.data.repository;

import com.zqykj.infrastructure.util.ClassTypeInformation;
import com.zqykj.infrastructure.util.TypeInformation;
import com.zqykj.tldw.aggregate.data.repository.elasticsearch.ElasticsearchRepositoryMetadata;
import com.zqykj.tldw.aggregate.searching.BaseOperations;
import org.springframework.core.KotlinDetector;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.List;

/**
 * <h1> 抽象repository interface 元数据 </h1>
 */
public abstract class AbstractRepositoryMetadata implements RepositoryMetadata {

    private final TypeInformation<?> typeInformation;
    private final Class<?> repositoryInterface;


    public AbstractRepositoryMetadata(Class<?> repositoryInterface) {

        this.repositoryInterface = repositoryInterface;
        this.typeInformation = ClassTypeInformation.from(repositoryInterface);
    }

    /**
     * Creates a new {@link RepositoryMetadata} for the given repository interface.
     *
     * @param repositoryInterface must not be {@literal null}.
     */
    public static RepositoryMetadata getMetadata(Class<?> repositoryInterface) {

        Assert.notNull(repositoryInterface, "Repository interface must not be null!");

        return BaseOperations.class.isAssignableFrom(repositoryInterface) ? new ElasticsearchRepositoryMetadata(repositoryInterface)
                : null;
    }

    @Override
    public TypeInformation<?> getReturnType(Method method) {

        TypeInformation<?> returnType = null;
        if (KotlinDetector.isKotlinType(method.getDeclaringClass())) {

            // the last parameter is Continuation<? super T> or Continuation<? super Flow<? super T>>
            List<TypeInformation<?>> types = typeInformation.getParameterTypes(method);
            returnType = types.get(types.size() - 1).getComponentType();
        }

        if (returnType == null) {
            returnType = typeInformation.getReturnType(method);
        }

        return returnType;
    }

    public Class<?> getReturnedDomainClass(Method method) {

        TypeInformation<?> returnType = getReturnType(method);

        return returnType.getType();
    }

    public Class<?> getRepositoryInterface() {
        return this.repositoryInterface;
    }
}
