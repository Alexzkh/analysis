/**
 * @作者 Mcj
 */
package com.zqykj.core.aggregation.query.builder;


import com.zqykj.parameters.query.QueryGeneralParameters;
import com.zqykj.parameters.query.QueryParameters;
import com.zqykj.core.aggregation.util.ClassNameForBeanClass;
import com.zqykj.core.aggregation.util.query.QueryNameForBeanClass;
import com.zqykj.util.ReflectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ElasticsearchException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <h1> es 的 dsl builder 构建 </h1>
 */
@Slf4j
public class QueryMappingBuilder {

    private static Map<String, Class<?>> dslNameForClass = new ConcurrentHashMap<>();

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

    public static Object buildDslQueryBuilderMapping(QueryParameters parameters) {

        try {
            // TODO 查询类型需要翻译 成对应数据源有的查询类型
            parameters.setType(parameters.queryTypeConvert(parameters.getType()));
            Class<?> queryClass = dslNameForClass.get(parameters.getType());
            //
            if (null == queryClass) {
                log.warn("could not find this dsl type!");
                throw new IllegalArgumentException("could not find this dsl type!");
            }
            Optional<Constructor<?>> constructor = ReflectionUtils.findConstructor(queryClass, parameters.getField(), parameters.getValue());
            Object target = null;
            if (constructor.isPresent()) {

                target = ReflectionUtils.getTargetInstanceViaReflection(constructor, queryClass, parameters.getField(), parameters.getValue());
            } else {

                // TODO 特殊查询构造函数 需要单独处理
            }
            return mapQuery(target, parameters, queryClass);
        } catch (Exception e) {
            log.error("could not build dsl query, error msg = {}", e.getMessage());
            throw new ElasticsearchException("could not build dsl query", e);
        }
    }

    private static Object mapQuery(Object target, QueryParameters parameters, Class<?> queryClass) {

        org.springframework.util.ReflectionUtils.doWithFields(parameters.getClass(), field -> {

            try {
                buildingQueryViaField(target, parameters, queryClass, field);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("error mapping property with name = {}", field.getName());
            }
        });

        return target;
    }

    private static void buildingQueryViaField(Object target, QueryParameters parameters, Class<?> queryClass,
                                              Field field) {

        if (QueryGeneralParameters.class.isAssignableFrom(field.getType())) {

            addGeneralParametersMapping(target, parameters, queryClass, field.getType());
        }
    }


    private static void addGeneralParametersMapping(Object target, QueryParameters parameters, Class<?> queryClass,
                                                    Class<?> fieldClass) {
        if (null == parameters) {
            return;
        }
        org.springframework.util.ReflectionUtils.doWithFields(fieldClass, subField -> {

            applyDefaultField(target, queryClass, subField, parameters);
        });
    }

    private static void applyDefaultField(Object target, Class<?> aggregationClass, Field subField, QueryParameters parameters) {

        Optional<Method> optionalMethod = ReflectionUtils.findMethod(aggregationClass, subField.getName(), subField.getType());
        // 开始调用此聚合类的方法, 为target 赋值
        optionalMethod.ifPresent(method -> {
            org.springframework.util.ReflectionUtils.makeAccessible(subField);
            Object value = org.springframework.util.ReflectionUtils.getField(subField, parameters.getGeneralParameters());
            if (null != value) {
                org.springframework.util.ReflectionUtils.invokeMethod(method, target, value);
            }
        });
    }
}
