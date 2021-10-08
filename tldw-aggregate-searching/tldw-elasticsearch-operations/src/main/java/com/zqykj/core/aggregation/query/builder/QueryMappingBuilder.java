/**
 * @作者 Mcj
 */
package com.zqykj.core.aggregation.query.builder;


import com.zqykj.common.enums.QueryType;
import com.zqykj.parameters.query.CombinationQueryParams;
import com.zqykj.parameters.query.CommonQueryParams;
import com.zqykj.parameters.query.QuerySpecialParams;
import com.zqykj.core.aggregation.util.ClassNameForBeanClass;
import com.zqykj.core.aggregation.util.query.QueryNameForBeanClass;
import com.zqykj.util.ReflectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * <h1> es 的 dsl builder 构建 </h1>
 */
@Slf4j
public class QueryMappingBuilder {

    private static Map<String, Class<?>> dslNameForClass = new ConcurrentReferenceHashMap<>(256);

    private static final Class<?> COMBINATION_QUERY_METHOD_TYPE = QueryBuilder.class;

    static {
        dslNameForClass.putAll(new QueryNameForBeanClass().getAggregateNameForClass());
    }

    public QueryMappingBuilder(ClassNameForBeanClass nameForBeanClass) {
        dslNameForClass = nameForBeanClass.getAggregateNameForClass();
    }

    public QueryMappingBuilder(ClassNameForBeanClass... nameForBeanClass) {
        for (ClassNameForBeanClass forBeanClass : nameForBeanClass) {
            dslNameForClass.putAll(forBeanClass.getAggregateNameForClass());
        }
    }

    public static Object buildDslQueryBuilderMapping(QuerySpecialParams params) {

        try {
            if (CollectionUtils.isEmpty(dslNameForClass)) {
                throw new IllegalArgumentException("dslNameForClass is empty!");
            }
            // 单个查询 与 组合查询不能同时存在
            if (null != params.getCommonQuery() && !CollectionUtils.isEmpty(params.getCombiningQuery())) {
                throw new IllegalArgumentException("Cannot use [commonQuery] with [combiningQuery] configuration option.");
            }
            Field[] declaredFields = ReflectionUtils.getDeclaredFields(params.getClass());
            Object target = null;
            for (Field field : declaredFields) {
                target = buildingQueryViaField(params, field);
            }
            return target;
        } catch (Exception e) {
            log.error("could not build dsl query, error msg = {}", e.getMessage());
            throw new ElasticsearchException("could not build dsl query", e);
        }
    }


    private static Object buildingQueryViaField(QuerySpecialParams parameters, Field field) throws ClassNotFoundException {

        // 单个查询 和 组合查询只会构建一个
        if (CommonQueryParams.class.isAssignableFrom(field.getType()) && null != parameters.getCommonQuery()) {

            // 单个查询
            return addCommonQueryMapping(parameters.getCommonQuery());

        } else if (List.class.isAssignableFrom(field.getType()) && !CollectionUtils.isEmpty(parameters.getCombiningQuery())) {

            Optional<Class<?>> parameterType = ReflectionUtils.findParameterType(parameters.getClass(), field.getName());
            if (!parameterType.isPresent()) {
                return null;
            }
            if (CommonQueryParams.class.isAssignableFrom(parameterType.get())) {
                // 组合查询
                return addCombinationQueryMapping(parameters.getCombiningQuery(), field.getType());
            }
        } else {

            // TODO
        }
        return null;
    }

    /**
     * <h2> 添加单个查询的参数映射 </h2>
     */
    private static Object addCommonQueryMapping(CommonQueryParams parameters) {
        if (null == parameters) {
            return null;
        }
        return applyDefaultField(parameters);
    }

    /**
     * <h2> 添加组合查询的参数映射 </h2>
     */
    private static Object addCombinationQueryMapping(List<CombinationQueryParams> combiningQuery, Class<?> fielsClass) {

        Class<?> queryTypeClass = getQueryTypeClass(QueryType.bool.toString());
        // 组合查询实例
        Object target = ReflectionUtils.getTargetInstanceViaReflection(queryTypeClass);

        for (CombinationQueryParams params : combiningQuery) {

            String methodName = params.convert(params.getType());
            Optional<Method> methodOptional = ReflectionUtils.findMethod(queryTypeClass, methodName, COMBINATION_QUERY_METHOD_TYPE);
            methodOptional.ifPresent(method -> {

                // 处理组合查询中的每一个单独查询参数
                dealWithCombination(method, target, params.getCombinationQuery());
            });
        }

        return target;
    }

    /**
     * <h2> 添加组合查询的参数
     * <p>
     * BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
     * boolQueryBuilder.must().must();
     * boolQueryBuilder.should().should();
     * boolQueryBuilder.filter().filter();
     * boolQueryBuilder.mustNot().mustNot();
     * </h2>
     */
    protected static void dealWithCombination(Method method, Object target, List<CommonQueryParams> commonQueryParams) {

        for (CommonQueryParams common : commonQueryParams) {

            Object single = addCommonQueryMapping(common);

            org.springframework.util.ReflectionUtils.invokeMethod(method, target, single);
        }
    }

    private static Object applyDefaultField(CommonQueryParams common) {

        // TODO 后续需要将 parameters.getType() 翻译成 es 的 查询类型
        // 获取query class
        Class<?> queryClass = getQueryTypeClass(common.getType().toString());

        // 获取一个查询类的实例
        return getQueryTypeInstance(queryClass, common);
    }

    private static Object getQueryTypeInstance(Class<?> queryClass, CommonQueryParams common) {

        Optional<Constructor<?>> constructor = ReflectionUtils.findConstructor(queryClass, common.getField(), common.getValue());
        Object target;
        if (constructor.isPresent()) {

            target = ReflectionUtils.getTargetInstanceViaReflection(queryClass, common.getField(), common.getValue());
        } else {

            target = buildSpecialAggregationConstructor(queryClass, common);
            if (null == target) {
                throw new IllegalArgumentException("could not find match aggregation constructor!");
            }
        }
        return target;
    }

    private static Object buildSpecialAggregationConstructor(Class<?> queryClass, CommonQueryParams common) {
        //TODO  一些特殊的查询类
        return null;
    }

    private static Class<?> getQueryTypeClass(String type) {

        Class<?> queryClass = dslNameForClass.get(type);
        if (null == queryClass) {
            log.warn("could not find this dsl type!");
            throw new IllegalArgumentException("could not find this dsl type!");
        }
        return queryClass;
    }
}
