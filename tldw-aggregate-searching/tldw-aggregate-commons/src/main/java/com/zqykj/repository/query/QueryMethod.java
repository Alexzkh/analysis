/**
 * @作者 Mcj
 */
package com.zqykj.repository.query;

import com.zqykj.domain.Page;
import com.zqykj.domain.Pageable;
import com.zqykj.domain.Sort;
import com.zqykj.repository.core.EntityMetadata;
import com.zqykj.repository.core.RepositoryMetadata;
import com.zqykj.repository.util.ClassUtils;
import com.zqykj.repository.util.QueryExecutionConverters;
import com.zqykj.util.ClassTypeInformation;
import com.zqykj.util.Lazy;
import com.zqykj.util.TypeInformation;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Stream;

/**
 * <h1> query method 抽象 </h1>
 */
public class QueryMethod {

    private final RepositoryMetadata metadata;
    private final Method method;
    private final Class<?> unwrappedReturnType;
    private final Parameters<?, ?> parameters;
    private final Lazy<Class<?>> domainClass;
    private final Lazy<Boolean> isCollectionQuery;

    public QueryMethod(Method method, RepositoryMetadata metadata) {

        Assert.notNull(method, "Method must not be null!");
        Assert.notNull(metadata, "Repository metadata must not be null!");

        this.method = method;
        this.unwrappedReturnType = potentiallyUnwrapReturnTypeFor(metadata, method);
        this.parameters = createParameters(method);
        this.metadata = metadata;

        if (ClassUtils.hasParameterOfType(method, Pageable.class)) {

            if (!isStreamQuery()) {
                assertReturnTypeAssignable(method, QueryExecutionConverters.getAllowedPageableTypes());
            }

            if (ClassUtils.hasParameterOfType(method, Sort.class)) {
                throw new IllegalStateException(String.format("Method must not have Pageable *and* Sort parameter. "
                        + "Use sorting capabilities on Pageable instead! Offending method: %s", method.toString()));
            }
        }

        Assert.notNull(this.parameters,
                () -> String.format("Parameters extracted from method '%s' must not be null!", method.getName()));

        if (isPageQuery()) {
            Assert.isTrue(this.parameters.hasPageableParameter(),
                    String.format("Paging query needs to have a Pageable parameter! Offending method %s", method.toString()));
        }

        this.domainClass = Lazy.of(() -> {

            Class<?> repositoryDomainClass = metadata.getDomainType();
            if (null == repositoryDomainClass) {
                repositoryDomainClass = DefaultDomain.class;
            }
            Class<?> methodDomainClass = metadata.getReturnedDomainClass(method);

            return repositoryDomainClass.isAssignableFrom(methodDomainClass)
                    ? methodDomainClass
                    : repositoryDomainClass;
        });

        this.isCollectionQuery = Lazy.of(this::calculateIsCollectionQuery);
    }

    public String getName() {
        return method.getName();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public EntityMetadata<?> getEntityInformation() {
        return () -> (Class) getDomainClass();
    }

    /**
     * <h2> Returns the name of the named query this method belongs to. </h2>
     */
    public String getNamedQueryName() {
        return String.format("%s.%s", getDomainClass().getSimpleName(), method.getName());
    }

    /**
     * <h2> 计算是否是 集合类型 </h2>
     */
    private boolean calculateIsCollectionQuery() {

        if (isPageQuery()) {
            return false;
        }

        Class<?> returnType = metadata.getReturnType(method).getType();

        if (QueryExecutionConverters.supports(returnType) && !QueryExecutionConverters.isSingleValue(returnType)) {
            return true;
        }

        if (QueryExecutionConverters.supports(unwrappedReturnType)) {
            return !QueryExecutionConverters.isSingleValue(unwrappedReturnType);
        }

        return ClassTypeInformation.from(unwrappedReturnType).isCollectionLike();
    }

    /**
     * <h2> 可能需要展开的返回类型 </h2>
     */
    private static Class<? extends Object> potentiallyUnwrapReturnTypeFor(RepositoryMetadata metadata, Method method) {

        TypeInformation<?> returnType = metadata.getReturnType(method);
        if (QueryExecutionConverters.supports(returnType.getType())) {

            // unwrap only one level to handle cases like Future<List<Entity>> correctly.

            TypeInformation<?> componentType = returnType.getComponentType();

            if (componentType == null) {
                throw new IllegalStateException(
                        String.format("Couldn't find component type for return value of method %s!", method));
            }

            return componentType.getType();
        }

        return returnType.getType();
    }

    protected Parameters<?, ?> createParameters(Method method) {
        return new DefaultParameters(method);
    }

    /**
     * <h2> 未包装的类型是否是Stream 类型 </h2>
     */
    public boolean isStreamQuery() {
        return Stream.class.isAssignableFrom(unwrappedReturnType);
    }

    public Class<?> getReturnedObjectType() {
        return metadata.getReturnedDomainClass(method);
    }

    /**
     * <h2> 未包装的类型是否是 Page类型 </h2>
     */
    public final boolean isPageQuery() {
        return org.springframework.util.ClassUtils.isAssignable(Page.class, unwrappedReturnType);
    }

    protected Class<?> getDomainClass() {
        return domainClass.get();
    }

    public boolean isCollectionQuery() {
        return isCollectionQuery.get();
    }

    public Parameters<?, ?> getParameters() {
        return parameters;
    }

    /**
     * <h2> 判断返回 类型是否可分配 </h2>
     */
    private static void assertReturnTypeAssignable(Method method, Set<Class<?>> types) {

        Assert.notNull(method, "Method must not be null!");
        Assert.notEmpty(types, "Types must not be null or empty!");

        // method return type of TypeInformation
        TypeInformation<?> returnType = ClassTypeInformation.fromReturnTypeOf(method);

        returnType = QueryExecutionConverters.isSingleValue(returnType.getType())
                ? returnType.getRequiredComponentType()
                : returnType;

        for (Class<?> type : types) {
            if (type.isAssignableFrom(returnType.getType())) {
                return;
            }
        }

        throw new IllegalStateException("Method has to have one of the following return types! " + types.toString());
    }
}
