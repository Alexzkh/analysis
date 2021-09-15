/**
 * @作者 Mcj
 */
package com.zqykj.repository.core.support;

import com.zqykj.repository.core.RepositoryInformation;
import com.zqykj.repository.core.RepositoryMetadata;
import com.zqykj.util.Streamable;
import com.zqykj.util.TypeInformation;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * <h1> Default implementation of {@link RepositoryInformation}. </h1>
 */
class DefaultRepositoryInformation implements RepositoryInformation {

    private final RepositoryMetadata metadata;
    private final Class<?> repositoryBaseClass;

    /**
     * Creates a new {@link DefaultRepositoryMetadata} for the given repository interface and repository base class.
     *
     * @param metadata            must not be {@literal null}.
     * @param repositoryBaseClass must not be {@literal null}.
     */
    public DefaultRepositoryInformation(RepositoryMetadata metadata, Class<?> repositoryBaseClass) {

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
    public Class<?> getRepositoryBaseClass() {
        return this.repositoryBaseClass;
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

    @Override
    public Streamable<Method> getQueryMethods() {

        Set<Method> result = new HashSet<>();

        for (Method method : getRepositoryInterface().getMethods()) {
            method = ClassUtils.getMostSpecificMethod(method, getRepositoryInterface());
            if (isQueryMethodCandidate(method)) {
                result.add(method);
            }
        }

        return Streamable.of(Collections.unmodifiableSet(result));
    }

    /**
     * <h2> eg. 一个子类在继承（或实现）一个父类（或接口）的泛型方法时，在子类中明确指定了泛型类型，
     * 那么在编译时编译器会自动生成桥接方法（当然还有其他情况会生成桥接方法 </h2>
     */
    private boolean isQueryMethodCandidate(Method method) {
        return !method.isBridge() && !method.isDefault()
                && !Modifier.isStatic(method.getModifiers());
    }


}
