/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.data.query;

import com.zqykj.domain.page.Page;
import com.zqykj.domain.page.Pageable;
import com.zqykj.domain.page.Sort;
import com.zqykj.domain.routing.Route;
import com.zqykj.infrastructure.util.ClassTypeInformation;
import com.zqykj.infrastructure.util.QueryExecutionConverters;
import com.zqykj.infrastructure.util.TypeInformation;
import com.zqykj.tldw.aggregate.data.repository.RepositoryInformation;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.ResolvableType;
import org.springframework.util.ClassUtils;
import org.springframework.util.NumberUtils;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 *
 */
public abstract class AbstractAggregateRepositoryQuery implements AggregateRepositoryQuery {

    // 该正则表达式 用来匹配@Query 注解内 value 值
    protected static final Pattern PARAMETER_PLACEHOLDER = Pattern.compile("\\?(\\d+)");
    protected static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();
    protected final Method method;
    protected final RepositoryInformation repositoryInformation;

    public AbstractAggregateRepositoryQuery(Method method, RepositoryInformation repositoryInformation) {
        this.method = method;
        this.repositoryInformation = repositoryInformation;
    }

    protected String replacePlaceHolders(String input, Object[] parameters) {

        Matcher matcher = PARAMETER_PLACEHOLDER.matcher(input);
        String result = input;
        while (matcher.find()) {
            String placeholder = Pattern.quote(matcher.group()) + "(?!\\d+)";
            // 获取占位符指定的方法参数的坐标
            Integer index = NumberUtils.parseNumber(matcher.group(1), Integer.class);
            result = result.replaceAll(placeholder, getParameterWithIndex(index, parameters));
        }
        return result;
    }

    private String getParameterWithIndex(int index, Object[] parameters) {
        if (index < 0 || index > parameters.length - 1) {
            throw new IllegalArgumentException("Invalid parameter index! You seem to have declared too little query method parameters!");
        }
        Object parameter = parameters[index];
        if (null == parameter) {
            return "null";
        }
        return parameter.toString();
    }

    /**
     * <h2> 方法返回类型是否是Page<?> 类型包装 </h2>
     */
    protected boolean isPageQuery() {

        return ClassUtils.isAssignable(Page.class, potentiallyUnwrapReturnTypeFor(this.method));
    }

    /**
     * <h2> 方法返回类型是否是Collection<?> 类型包装 </h2>
     */
    protected boolean isCollectionQuery() {

        return calculateIsCollectionQuery();
    }

    /**
     * <h2> 方法返回类型是否是 Stream类型包装</h2>
     */
    protected boolean isStreamQuery() {
        return Stream.class.isAssignableFrom(potentiallyUnwrapReturnTypeFor(this.method));
    }

    private static Class<?> potentiallyUnwrapReturnTypeFor(Method method) {

        TypeInformation<?> returnType = ClassTypeInformation.fromReturnTypeOf(method);
        if (QueryExecutionConverters.supports(returnType.getType())) {
            // unwrap only one level to handle cases like Future<List<Entity>> correctly.
            TypeInformation<?> componentType = returnType.getComponentType();

            if (componentType == null) {
                throw new IllegalStateException(
                        String.format("Couldn't find component type for return value of method %s!", method));
            }

            return componentType.getType();
        }
        // 返回
        return returnType.getType();
    }

    /**
     * <h2> 检查返回类型是否是Collection 类型 </h2>
     */
    private boolean calculateIsCollectionQuery() {

        if (isPageQuery()) {
            return false;
        }
        Class<?> unwrappedReturnType = potentiallyUnwrapReturnTypeFor(this.method);

        Class<?> returnType = repositoryInformation.getReturnType(method).getType();

        if (QueryExecutionConverters.supports(returnType) && !QueryExecutionConverters.isSingleValue(returnType)) {
            return true;
        }

        if (QueryExecutionConverters.supports(unwrappedReturnType)) {
            return !QueryExecutionConverters.isSingleValue(unwrappedReturnType);
        }
        return ClassTypeInformation.from(unwrappedReturnType).isCollectionLike();
    }


    /**
     * <h2> 获取method参数 Pageable 参数值 </h2>
     */
    protected Pageable getPageable(Object[] parameters) {

        // 获取Method Pageable 参数 Index
        Integer index = getSpecifiedParameterTypeIndex(Pageable.class);
        if (null == index) {
            return Pageable.unpaged();
        }
        return parameters[index] == null ? Pageable.unpaged() : (Pageable) parameters[index];
    }

    protected Route getRouting(Object[] parameters) {

        // 获取Method Pageable 参数 Index
        Integer index = getSpecifiedParameterTypeIndex(Route.class);
        if (null == index) {
            return Route.unRoute();
        }
        return parameters[index] == null ? Route.unRoute() : (Route) parameters[index];
    }

    protected Sort getSort(Object[] parameters) {

        // 获取Method Pageable 参数 Index
        Integer index = getSpecifiedParameterTypeIndex(Sort.class);
        if (null == index) {
            return Sort.unsorted();
        }
        return parameters[index] == null ? Sort.unsorted() : (Sort) parameters[index];
    }

    private Integer getSpecifiedParameterTypeIndex(Class<?> clazz) {

        int parameterCount = method.getParameterCount();

        for (int i = 0; i < parameterCount; i++) {

            MethodParameter methodParameter = new MethodParameter(method, i);
            methodParameter.initParameterNameDiscovery(PARAMETER_NAME_DISCOVERER);

            Class<?> parameter = potentiallyUnwrapParameterType(methodParameter);

            // 用来给SearchRequest 设置分页信息, 此时clazz 为 Pageable
            if (clazz.isAssignableFrom(parameter)) {
                return i;
            }

            // 用来给SearchRequest 设置排序, 此时clazz 为 Sort
            if (clazz.isAssignableFrom(parameter)) {
                return i;
            }
            // 用来给SearchRequest 设置routing,此时clazz 为 Routing
            if (clazz.isAssignableFrom(parameter)) {
                return i;
            }
        }
        return null;
    }

    private static Class<?> potentiallyUnwrapParameterType(MethodParameter parameter) {

        Class<?> originalType = parameter.getParameterType();

        if (QueryExecutionConverters.supports(parameter.getParameterType()) &&
                QueryExecutionConverters.supportsUnwrapping(parameter.getParameterType())) {
            return ResolvableType.forMethodParameter(parameter).getGeneric(0).resolve(Object.class);
        }
        return originalType;
    }

}
