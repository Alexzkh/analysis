/**
 * @作者 Mcj
 */
package com.zqykj.core.aggregation.query.builder;


import com.zqykj.common.enums.QueryType;
import com.zqykj.parameters.query.*;
import com.zqykj.core.aggregation.util.ClassNameForBeanClass;
import com.zqykj.core.aggregation.util.query.QueryNameForBeanClass;
import com.zqykj.util.ReflectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * <h1> es 的 dsl builder 构建 </h1>
 */
@Slf4j
public class QueryMappingBuilder {

    private static Map<String, Class<?>> dslNameForClass = new ConcurrentReferenceHashMap<>(256);

    private static final Class<?> COMBINATION_QUERY_METHOD_TYPE = QueryBuilder.class;

    private static final String FROM_METHOD_NAME = "from";
    private static final String TO_METHOD_NAME = "to";

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
            if (null == params) {
                return null;
            }
            if (CollectionUtils.isEmpty(dslNameForClass)) {
                throw new IllegalArgumentException("dslNameForClass is empty!");
            }
            // 单个查询 与 组合查询不能同时存在
            if (null != params.getCommonQuery() && !CollectionUtils.isEmpty(params.getCombiningQuery())) {
                throw new IllegalArgumentException("Cannot use [commonQuery] with [combiningQuery] configuration option.");
            }
            return buildingQueryViaField(params);
        } catch (Exception e) {
            log.error("could not build dsl query, error msg = {}", e.getMessage());
            throw new ElasticsearchException("could not build dsl query", e);
        }
    }


    private static Object buildingQueryViaField(QuerySpecialParams params) {

        Object target = null;
        if (null != params.getCommonQuery()) {
            // 单个查询
            target = addCommonQueryMapping(params.getCommonQuery());
        } else if (!CollectionUtils.isEmpty(params.getCombiningQuery())) {
            // 组合查询

            target = addCombinationQueryMapping(params.getCombiningQuery());
        }

        // 通用参数设置
        DefaultQueryParam defaultParam = params.getDefaultParam();
        if (null != defaultParam && null != target) {

            Field[] declaredFields = ReflectionUtils.getDeclaredFields(defaultParam.getClass());
            for (Field field : declaredFields) {

                org.springframework.util.ReflectionUtils.makeAccessible(field);
                Object value = org.springframework.util.ReflectionUtils.getField(field, defaultParam);
                Optional<Method> methodOptional = ReflectionUtils.findMethod(target.getClass(), field.getName(), field.getType());
                Object finalTarget = target;
                methodOptional.ifPresent(method -> org.springframework.util.ReflectionUtils.invokeMethod(method, finalTarget, value));
            }
        }
        return target;
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
    private static Object addCombinationQueryMapping(List<CombinationQueryParams> combiningQuery) {

        Class<?> queryTypeClass = getQueryTypeClass(QueryType.bool.toString());
        // 组合查询实例
        Object target = ReflectionUtils.getTargetInstanceViaReflection(queryTypeClass);

        for (CombinationQueryParams params : combiningQuery) {

            String methodName = params.convert(params.getType());
            Optional<Method> methodOptional = ReflectionUtils.findMethod(queryTypeClass, methodName, COMBINATION_QUERY_METHOD_TYPE);
            methodOptional.ifPresent(method -> {

                // 处理组合查询中的每一个单独查询参数
                dealWithCombination(method, target, params.getCommonQueryParams());
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

            // 如果还是 bool 查询(需要特殊处理)
            Object perTarget;
            if (null != common.getCompoundQueries()) {

                // 仍然是bool 对象
                perTarget = addCombinationQueryMapping(Collections.singletonList(common.getCompoundQueries()));
            } else {
                // 单个对象
                perTarget = addCommonQueryMapping(common);
            }
            org.springframework.util.ReflectionUtils.invokeMethod(method, target, perTarget);
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

        Object target = null;
        // 1. 范围查询 Range -> 针对一些日期、数值的范围匹配
        if (RangeQueryBuilder.class.isAssignableFrom(queryClass)) {

            // 范围查询(对于日期、数值类的范围)
            if (null != common.getDateRange()) {
                DateRange dateRange = common.getDateRange();

                target = ReflectionUtils.getTargetInstanceViaReflection(queryClass, common.getField());

                applyDateRange(queryClass, dateRange, target);
            } else if (null != common.getQueryOperator()) {

                // 数值类的范围查询
                QueryOperator queryOperator = common.getQueryOperator();
                target = ReflectionUtils.getTargetInstanceViaReflection(queryClass, common.getField());
                applyNumericalValueRange(queryClass, queryOperator, target, common.getValue());
            }
        }

        return target;
    }


    /**
     * <h2> 数值范围处理 </h2>
     */
    private static void applyNumericalValueRange(Class<?> queryClass, QueryOperator operator, Object target, Object value) {

        String methodName = getRangeMethodNameConvertFromOperator(operator);

        if (QueryOperator.eq.name().equals(methodName)) {

            Optional<Method> methodFromOptional = ReflectionUtils.findMethod(queryClass, FROM_METHOD_NAME, Object.class);
            methodFromOptional.ifPresent(fromMethod -> org.springframework.util.ReflectionUtils.invokeMethod(fromMethod, target, value));
            Optional<Method> methodToOptional = ReflectionUtils.findMethod(queryClass, TO_METHOD_NAME, Object.class);
            methodToOptional.ifPresent(toMethod -> org.springframework.util.ReflectionUtils.invokeMethod(toMethod, target, value));
        } else {

            Optional<Method> methodOptional = ReflectionUtils.findMethod(queryClass, methodName, Object.class);
            methodOptional.ifPresent(method -> org.springframework.util.ReflectionUtils.invokeMethod(method, target, value));
        }
    }


    /**
     * <h2> 日期范围处理 </h2>
     */
    private static void applyDateRange(Class<?> queryClass, DateRange dateRange, Object target) {

        org.springframework.util.ReflectionUtils.doWithFields(dateRange.getClass(), field -> {

            Optional<Method> methodOptional = ReflectionUtils.findMethod(queryClass, field.getName(), field.getType());

            methodOptional.ifPresent(method -> {
                org.springframework.util.ReflectionUtils.makeAccessible(field);
                Object value = org.springframework.util.ReflectionUtils.getField(field, dateRange);
                org.springframework.util.ReflectionUtils.invokeMethod(method, target, value);
            });
        });
    }

    /**
     * <h2> 根据操作符 获取对应方法名称  eg. 大于 -> gt, 大于等于 -> gte, 小于 -> lt,小于等于 -> lte </h2>
     */
    private static String getRangeMethodNameConvertFromOperator(QueryOperator operator) {

        return operator.name();
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
