/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.data.query;

import com.zqykj.domain.page.Page;
import com.zqykj.domain.page.Pageable;
import com.zqykj.domain.page.Sort;
import com.zqykj.infrastructure.util.ClassTypeInformation;
import com.zqykj.infrastructure.util.TypeInformation;
import com.zqykj.tldw.aggregate.data.support.AggregateRepositoryInformation;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.NumberUtils;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * @author Mcj
 */
@Setter
@Getter
public abstract class AbstractAggregateRepositoryQuery implements AggregateRepositoryQuery {

    public static final String ENABLE_TYPE = "enable.datasource.type";
    public static final String MONGODBTYPE = "mongodb";
    public static final String ELASTICSEARCHTYPE = "elasticsearch";
    // 该正则表达式 用来匹配@Query 注解内 value 值
    protected static final Pattern PARAMETER_PLACEHOLDER = Pattern.compile("\\?(\\d+)");
    // @Query 注解内 value 值
    private String query;
    private Method method;
    private AggregateRepositoryInformation repositoryInformation;
    private static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    @Override
    @Nullable
    public abstract Object execute(Object[] parameters, Method method);

    @Override
    public abstract Method getQueryMethod();

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
        if (index < parameters.length - 1 || index > parameters.length - 1) {
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

        return ClassUtils.isAssignable(Page.class, potentiallyUnwrapReturnTypeFor(method));
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
        return Stream.class.isAssignableFrom(potentiallyUnwrapReturnTypeFor(method));
    }

    private static Class<?> potentiallyUnwrapReturnTypeFor(Method method) {

        TypeInformation<?> returnType = ClassTypeInformation.fromReturnTypeOf(method);
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
        Class<?> unwrappedReturnType = potentiallyUnwrapReturnTypeFor(method);
        return ClassTypeInformation.from(unwrappedReturnType).isCollectionLike();
    }

    /**
     * <h2> 获取method参数 Pageable 参数值 </h2>
     */
    protected Pageable getPageable(Object[] parameters) {

        // 获取Method Pageable 参数 Index
        Integer index = getSpecifiedParameterTypeIndex();
        if (null == index) {
            return Pageable.unpaged();
        }
        return parameters[index] == null ? Pageable.unpaged() : (Pageable) parameters[index];
    }

    private Integer getSpecifiedParameterTypeIndex() {

        int parameterCount = method.getParameterCount();

        for (int i = 0; i < parameterCount; i++) {

            MethodParameter methodParameter = new MethodParameter(method, i);
            methodParameter.initParameterNameDiscovery(PARAMETER_NAME_DISCOVERER);

            Class<?> parameter = potentiallyUnwrapParameterType(methodParameter);

            if (Pageable.class.isAssignableFrom(parameter)) {
                return i;
            }

            if (Sort.class.isAssignableFrom(parameter)) {
                return i;
            }
        }
        return null;
    }

    private static Class<?> potentiallyUnwrapParameterType(MethodParameter parameter) {

        Class<?> originalType = parameter.getParameterType();

        // if (QueryExecutionConverters.supports(parameter.getParameterType()) &&
        //     QueryExecutionConverters.supportsUnwrapping(parameter.getParameterType())) {
        // return ResolvableType.forMethodParameter(parameter).getGeneric(0).resolve(Object.class);
        // }
        return originalType;
    }

}
