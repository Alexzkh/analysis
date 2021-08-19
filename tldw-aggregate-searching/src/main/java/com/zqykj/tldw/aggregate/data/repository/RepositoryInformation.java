/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.data.repository;


import com.zqykj.annotations.Query;
import com.zqykj.infrastructure.util.ClassTypeInformation;
import com.zqykj.infrastructure.util.TypeInformation;
import org.springframework.core.KotlinDetector;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <h1> Repository Information interface </h1>
 */
public abstract class RepositoryInformation implements RepositoryMetadata {

    private final List<Method> methods;
    private final TypeInformation<?> typeInformation;

    public RepositoryInformation(Class<?> repositoryInterface) {
        this.methods = Arrays.asList(repositoryInterface.getMethods());
        this.typeInformation = ClassTypeInformation.from(repositoryInterface);
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

    public TypeInformation<?> getReturnType(Method method) {

        TypeInformation<?> returnType;
//        if (KotlinDetector.isKotlinType(method.getDeclaringClass()) && KotlinReflectionUtils.isSuspend(method)) {
//
//            // the last parameter is Continuation<? super T> or Continuation<? super Flow<? super T>>
//            List<TypeInformation<?>> types = typeInformation.getParameterTypes(method);
//            returnType = types.get(types.size() - 1).getComponentType();
//        }
        returnType = typeInformation.getReturnType(method);
        return returnType;
    }

    public abstract Class<?> getRepositoryBaseClass();
}
