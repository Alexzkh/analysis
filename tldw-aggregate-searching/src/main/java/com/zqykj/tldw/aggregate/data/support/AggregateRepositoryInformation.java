/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.data.support;

import com.zqykj.annotations.Query;
import com.zqykj.tldw.aggregate.repository.TestRepository;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <h1> 聚合仓库接口信息 </h1>
 */
public class AggregateRepositoryInformation {

    private final Class<?> idType;

    private final Class<?> domainType;

    private final List<Method> methods;


    /**
     * <h2> create a new {@link AggregateRepositoryInformation} for the given repository interface. </h2>
     *
     * @param repositoryInterface must not be {@literal null}.
     */
    public AggregateRepositoryInformation(Class<?> repositoryInterface) {

        // 获取这个interface type information
        List<Type> interfaceGenericInformation = getInterfaceGenericInformation(repositoryInterface, TestRepository.class);
        assert interfaceGenericInformation != null;
        this.domainType = (Class<?>) interfaceGenericInformation.get(0);
        this.idType = (Class<?>) interfaceGenericInformation.get(1);
        this.methods = Arrays.asList(repositoryInterface.getMethods());
    }

    private List<Type> getInterfaceGenericInformation(Class<?> type, Class<?> superType) {

        if (!superType.isAssignableFrom(type)) {
            return null;
        }
        List<Type> candidates = new ArrayList<>(Arrays.asList(type.getGenericInterfaces()));
        List<Type> interfaceGenericParams = new ArrayList<>(2);
        for (Type candidate : candidates) {
            if (candidate instanceof ParameterizedType) {
                Type[] actualTypeArguments = ((ParameterizedType) candidate).getActualTypeArguments();
                interfaceGenericParams.addAll(Arrays.asList(actualTypeArguments));
            }
        }
        return interfaceGenericParams;
    }

    public Class<?> getIdType() {
        return this.idType;
    }

    public Class<?> getDomainType() {
        return this.domainType;
    }

    /**
     * <h2> 获取带有@Query 注解的方法 </h2>
     */
    public List<Method> getQueryMethods() {
        return methods.stream().filter(this::isQueryMethodCandidate).collect(Collectors.toList());
    }

    private boolean isQueryMethodCandidate(Method method) {
        return !method.isBridge() && !method.isDefault() //
                && !Modifier.isStatic(method.getModifiers()) //
                && (isQueryAnnotationPresentOn(method));
    }

    private boolean isQueryAnnotationPresentOn(Method method) {
        return AnnotationUtils.findAnnotation(method, Query.class) != null;
    }

    /**
     * <h2> 获取Method @Query 注解上的 value 值 </h2>
     */
    public String getAnnotatedQuery(Method method) {
        Optional<Query> optionalQuery = Optional.ofNullable(AnnotationUtils.getAnnotation(method, Query.class));
        return optionalQuery.map(Query::value).orElseThrow(() -> new RuntimeException("@Query value must not null!"));
    }
}
