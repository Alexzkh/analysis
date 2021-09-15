/**
 * @作者 Mcj
 */
package com.zqykj.repository.query;

import com.zqykj.domain.Pageable;
import com.zqykj.domain.Routing;
import com.zqykj.domain.Sort;
import com.zqykj.repository.util.QueryExecutionConverters;
import com.zqykj.util.ClassTypeInformation;
import com.zqykj.util.TypeInformation;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <h1> 抽象查询方法 单个参数类 </h1>
 */
public class Parameter {

    static final List<Class<?>> TYPES;

    private static final String NAMED_PARAMETER_TEMPLATE = ":%s";
    private static final String POSITION_PARAMETER_TEMPLATE = "?%s";

    private final MethodParameter parameter;
    private final Class<?> parameterType;
    private final boolean isDynamicProjectionParameter;

    static {

        List<Class<?>> types = new ArrayList<>(Arrays.asList(Pageable.class, Sort.class));

        TYPES = Collections.unmodifiableList(types);
    }

    protected Parameter(MethodParameter parameter) {

        Assert.notNull(parameter, "MethodParameter must not be null!");

        this.parameter = parameter;
        this.parameterType = potentiallyUnwrapParameterType(parameter);
        this.isDynamicProjectionParameter = isDynamicProjectionParameter(parameter);
    }

    /**
     * <h2> 返回参数是否是特殊参数 </h2>
     */
    public boolean isSpecialParameter() {
        return isDynamicProjectionParameter || TYPES.contains(parameter.getParameterType());
    }

    /**
     * <h2> 是否是动态投影参数 </h2>
     */
    public boolean isDynamicProjectionParameter() {
        return isDynamicProjectionParameter;
    }

    public int getIndex() {
        return parameter.getParameterIndex();
    }

    /**
     * Returns the type of the {@link Parameter}.
     *
     * @return the type
     */
    public Class<?> getType() {
        return parameterType;
    }

    /**
     * <h2> 是否是分页参数 </h2>
     */
    boolean isPageable() {
        return Pageable.class.isAssignableFrom(getType());
    }

    /**
     * <h2> 是否是路由参数 </h2>
     */
    boolean isRouting() {
        return Routing.class.isAssignableFrom(getType());
    }

    /**
     * <h2> 是否是排序参数 </h2>
     */
    boolean isSort() {
        return Sort.class.isAssignableFrom(getType());
    }

    /**
     * Returns whether the {@link Parameter} is to be bound to a query.
     */
    public boolean isBindable() {
        return !isSpecialParameter();
    }

    /**
     * <h2> 是否是动态投影参数 </h2>
     */
    private static boolean isDynamicProjectionParameter(MethodParameter parameter) {

        Method method = parameter.getMethod();

        if (method == null) {
            throw new IllegalStateException(String.format("Method parameter %s is not backed by a method!", parameter));
        }

        ClassTypeInformation<?> ownerType = ClassTypeInformation.from(parameter.getDeclaringClass());
        TypeInformation<?> parameterTypes = ownerType.getParameterTypes(method).get(parameter.getParameterIndex());

        if (!parameterTypes.getType().equals(Class.class)) {
            return false;
        }

        TypeInformation<?> bound = parameterTypes.getTypeArguments().get(0);
        TypeInformation<Object> returnType = ClassTypeInformation.fromReturnTypeOf(method);

        return bound.equals(QueryExecutionConverters.unwrapWrapperTypes(returnType));
    }

    private static boolean isWrapped(MethodParameter parameter) {
        return QueryExecutionConverters.supports(parameter.getParameterType());
    }

    private static boolean shouldUnwrap(MethodParameter parameter) {
        return QueryExecutionConverters.supportsUnwrapping(parameter.getParameterType());
    }

    /**
     * <h2> 如果是包装器类型 (如果能解开,则返回它的组件类型) Page<List<String>> </h2>
     */
    private static Class<?> potentiallyUnwrapParameterType(MethodParameter parameter) {

        Class<?> originalType = parameter.getParameterType();

        if (isWrapped(parameter) && shouldUnwrap(parameter)) {
            return ResolvableType.forMethodParameter(parameter).getGeneric(0).resolve(Object.class);
        }

        return originalType;
    }
}
