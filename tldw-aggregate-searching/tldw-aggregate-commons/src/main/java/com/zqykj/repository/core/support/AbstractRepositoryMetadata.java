/**
 * @作者 Mcj
 */
package com.zqykj.repository.core.support;

import com.zqykj.repository.Repository;
import com.zqykj.repository.core.RepositoryMetadata;
import com.zqykj.repository.util.QueryExecutionConverters;
import com.zqykj.util.ClassTypeInformation;
import com.zqykj.util.Optionals;
import com.zqykj.util.TypeInformation;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.KotlinDetector;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

/**
 * <h1> Base class for {@link RepositoryMetadata} implementations. </h1>
 */
public abstract class AbstractRepositoryMetadata implements RepositoryMetadata {

    private final TypeInformation<?> typeInformation;
    private final Class<?> repositoryInterface;


    public AbstractRepositoryMetadata(Class<?> repositoryInterface) {

        Assert.notNull(repositoryInterface, "Given type must not be null!");
        Assert.isTrue(repositoryInterface.isInterface(), "Given type must be an interface!");

        this.repositoryInterface = repositoryInterface;
        this.typeInformation = ClassTypeInformation.from(repositoryInterface);

        //  默认CRUD 方法的默认代理实现 (eg. findById、findByAll、deleteById....)
//        this.crudMethods = Lazy.of(() -> new DefaultCrudMethods(this));
    }

    /**
     * <h2> Looks up the most specific method for the given method and type and returns an accessible version of discovered </h2>
     * {@link Method} if found.
     *
     * @param method 给定的方法
     * @param type   Class<?>
     * @see ClassUtils#getMostSpecificMethod(Method, Class)
     */
    private static Optional<Method> getMostSpecificMethod(Method method, Class<?> type) {

        return Optionals.toStream(Optional.ofNullable(ClassUtils.getMostSpecificMethod(method, type)))//
                .map(BridgeMethodResolver::findBridgedMethod)//
                .peek(ReflectionUtils::makeAccessible)//
                .findFirst();
    }


    /**
     * <h2> Creates a new {@link RepositoryMetadata} for the given repository interface. </h2>
     *
     * @param repositoryInterface must not be {@literal null}.
     * @since 1.9
     */
    public static RepositoryMetadata getMetadata(Class<?> repositoryInterface) {

        Assert.notNull(repositoryInterface, "Repository interface must not be null!");

        // AnnotationRepositoryMetadata 可以使用注解 方式 指定特定的类为 Repository 类型
//        return Repository.class.isAssignableFrom(repositoryInterface) ? new DefaultRepositoryMetadata(repositoryInterface)
//                : new AnnotationRepositoryMetadata(repositoryInterface);
        return Repository.class.isAssignableFrom(repositoryInterface) ? new DefaultRepositoryMetadata(repositoryInterface) : null;
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

    /**
     * <h2> 获取method 返回值的 class </h2>
     */
    public Class<?> getReturnedDomainClass(Method method) {

        TypeInformation<?> returnType = getReturnType(method);

        return QueryExecutionConverters.unwrapWrapperTypes(returnType).getType();
    }

    /**
     * <h2> 获取repository class </h2>
     */
    public Class<?> getRepositoryInterface() {
        return this.repositoryInterface;
    }
}
