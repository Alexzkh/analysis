/**
 * @作者 Mcj
 */
package com.zqykj.core.aggregation.query;

import com.zqykj.core.aggregation.query.parameters.DateParameters;
import com.zqykj.core.aggregation.query.parameters.GeneralParameters;
import com.zqykj.core.aggregation.query.parameters.aggregate.AggregationParameters;
import com.zqykj.core.aggregation.query.parameters.pipeline.PipelineAggregationParameters;
import com.zqykj.core.aggregation.util.AggregationNameForBeanClass;
import com.zqykj.core.aggregation.util.bucket.AggregationNameForBeanClassOfBucket;
import com.zqykj.util.ReflectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.PipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.range.RangeAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.BucketScriptPipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.MaxBucketPipelineAggregationBuilder;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <h1> elasticsearch aggregation instance building </h1>
 * 注:  mongodb 等其他数据源的 aggregation instance building 自行补充
 */
@Slf4j
public class AggregationMappingBuilder {

    private static final String CALENDAR_INTERVAL = "calendarInterval";

    private static final String FIXED_INTERVAL = "fixedInterval";

    private static final String SCRIPT_PARAMETER = "script";

    private static final Class<?> SCRIPT_TYPE = Script.class;

    private static final Class<?> DATE_INTERVAL_TYPE = DateHistogramInterval.class;

    private static final String SUB_AGGREGATION = "subAggregation";

    private static final Class<?> SUB_AGGREGATION_TYPE = AggregationBuilder.class;
    private static final Class<?> SUB_AGGREGATION_TYPE_PIPELINE = PipelineAggregationBuilder.class;

    private Map<String, Class<?>> aggregateNameForClass = new ConcurrentHashMap<>();

    public AggregationMappingBuilder(AggregationNameForBeanClass nameForBeanClass) {
        this.aggregateNameForClass = nameForBeanClass.getAggregateNameForClass();
    }

    public AggregationMappingBuilder(AggregationNameForBeanClass... nameForBeanClass) {
        for (AggregationNameForBeanClass forBeanClass : nameForBeanClass) {
            this.aggregateNameForClass.putAll(forBeanClass.getAggregateNameForClass());
        }
    }

    /**
     * <h2> 构建AggregationBuilder 聚合 </h2>
     */
    public Object buildAggregationInstance(AggregationParameters parameters) {

        try {
            // TODO 聚合类型需要翻译 成对应数据源有的聚合类型
            // eg. max (对于es 来说有max, mongodb 可能不叫max)
            Class<?> aggregationClass = aggregateNameForClass.get(parameters.getType());
            //
            if (null == aggregationClass) {
                log.warn("could not find this aggregation type!");
                throw new IllegalArgumentException("could not find this aggregation type!");
            }

            // 看看此聚合类的构造函数是否可以填充 聚合名称
            Optional<Constructor<?>> constructor = ReflectionUtils.findConstructor(aggregationClass, parameters.getName());
            Object target = null;
            if (constructor.isPresent()) {

                target = ReflectionUtils.getTargetInstanceViaReflection(aggregationClass, parameters.getName());
            } else {
                // 例如es 的 filter, 不仅需要填充聚合名称, 还需要填充查询参数 QueryBuilder

            }

            return mapAggregation(target, parameters, aggregationClass);
        } catch (Exception e) {
            log.error("could not build aggregation, error msg = {}", e.getMessage());
            throw new ElasticsearchException("could not build aggregation", e);
        }
    }

    /**
     * <h2> 构建PipelineAggregationBuilder 聚合 </h2>
     */
    public Object buildPipelineAggregationInstance(PipelineAggregationParameters parameters) {

        try {
            // TODO 聚合类型需要翻译 成对应数据源有的聚合类型
            // eg. max (对于es 来说有max, mongodb 可能不叫max)
            Class<?> aggregationClass = aggregateNameForClass.get(parameters.getType());
            //
            if (null == aggregationClass) {
                log.warn("could not find this aggregation type!");
                throw new IllegalArgumentException("could not find this aggregation type!");
            }
            Optional<Constructor<?>> constructor;
            Object target = null;
            // 分2类, 一类是不需要script , 一类是需要的
            if (StringUtils.isBlank(parameters.getScript())) {

                constructor = ReflectionUtils.findConstructor(aggregationClass, parameters.getName(), parameters.getBucketsPath());
                if (constructor.isPresent()) {
                    target = ReflectionUtils.getTargetInstanceViaReflection(aggregationClass, parameters.getName(), parameters.getBucketsPath());
                }
            } else {

                Script script = new Script(parameters.getScript());
                constructor = ReflectionUtils.findConstructor(aggregationClass, parameters.getName(), script, parameters.getBucketsPathMap());
                if (constructor.isPresent()) {
                    target = ReflectionUtils.getTargetInstanceViaReflection(aggregationClass, parameters.getName(), script, parameters.getBucketsPathMap());
                }
            }
            return target;
        } catch (Exception e) {
            log.error("could not build pipeline aggregation, error msg = {}", e.getMessage());
            throw new ElasticsearchException("could not build pipeline aggregation", e);
        }
    }


    private Object mapAggregation(Object target, AggregationParameters parameters, Class<?> aggregationClass) {

        // 处理 AggregationParameters 中不同类型的参数, 为 aggregation instance target 赋值
        org.springframework.util.ReflectionUtils.doWithFields(parameters.getClass(), field -> {

            try {
                buildingAggregationViaField(target, parameters, aggregationClass, field);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("error mapping property with name = {}", field.getName());
            }
        });

        return target;
    }


    private void buildingAggregationViaField(Object target, AggregationParameters parameters, Class<?> aggregationClass,
                                             Field field) {

        if (GeneralParameters.class.isAssignableFrom(field.getType())) {

            addGeneraParametersMapping(target, parameters, aggregationClass, field, field.getType());
        } else if (DateParameters.class.isAssignableFrom(field.getType())) {

            addDateParametersMapping(target, parameters, aggregationClass, field, field.getType());
        } else if (List.class.isAssignableFrom(field.getType())) {
            // subAggregation 处理... 类型是 AggregationBuilder
            addSubAggregationMapping(target, parameters.getSubAggregation(), aggregationClass);
        }
    }

    private void addSubAggregationMapping(Object target, List<AggregationParameters> subParameters, Class<?> aggregationClass) {

        if (CollectionUtils.isEmpty(subParameters)) {
            return;
        }
        for (AggregationParameters subParameter : subParameters) {
            Object subTarget = buildAggregationInstance(subParameter);
            //
            applySubAggregation(target, aggregationClass, subTarget);
        }
    }


    @SuppressWarnings("all")
    private void addGeneraParametersMapping(Object target, AggregationParameters parameters,
                                            Class<?> aggregationClass, Field field, Class<?> fieldClass) {

        if (null == parameters.getGeneralParameters()) {
            return;
        }
        // 处理GeneraParameters 中的每一个字段
        org.springframework.util.ReflectionUtils.doWithFields(fieldClass, subField -> {

            // 根据field name 默认处理 (AggregationParameters 定义的field name 都是匹配 aggregationClass 的方法名称)
            // 后面如果mongodb 方法名称根据当前es 的不一致, 中间加一层转换即可
            applyDefaultField(target, aggregationClass, subField, parameters);
        });
    }

    @SuppressWarnings("all")
    private void addDateParametersMapping(Object target, AggregationParameters parameters,
                                          Class<?> aggregationClass, Field field, Class<?> fieldClass) {

        if (null == parameters.getDateParameters()) {
            return;
        }
        // 处理GeneraParameters 中的每一个字段
        org.springframework.util.ReflectionUtils.doWithFields(fieldClass, subField -> {

            // 检测aggregationClass 是否拥有 subField这个方法
            // 日期类型的需要特殊处理
            // 目前先处理 date histogram 的相关参数
            if (subField.getName().equals(AggregationMappingBuilder.CALENDAR_INTERVAL)) {

                // 首先看field value 是否为空
                org.springframework.util.ReflectionUtils.makeAccessible(subField);
                Object calendarIntervalValue = org.springframework.util.ReflectionUtils.getField(subField, parameters.getDateParameters());

                if (null != calendarIntervalValue) {
                    // 检测当前 aggregationClass 是否有此方法 (此方法的类型为 DateHistogramInterval)
                    applyDateInterval(target, aggregationClass, subField.getName(), calendarIntervalValue);
                }

            } else if (subField.getName().equals(AggregationMappingBuilder.FIXED_INTERVAL)) {

                // 检测当前 aggregationClass 是否有此方法 (此方法的类型为 DateHistogramInterval)
                org.springframework.util.ReflectionUtils.makeAccessible(subField);
                Object fixedIntervalValue = org.springframework.util.ReflectionUtils.getField(subField, parameters.getDateParameters());
                applyDateInterval(target, aggregationClass, subField.getName(), fixedIntervalValue);
            } else {
                // field 都正常按照名称处理
                applyDefaultField(target, aggregationClass, subField, parameters);
            }
        });
    }

    private void applySubAggregation(Object root, Class<?> aggregationClass, Object sub) {
        Optional<Method> optionalMethod = Optional.empty();
        if (SUB_AGGREGATION_TYPE.isAssignableFrom(sub.getClass())) {

            optionalMethod = ReflectionUtils.findMethod(aggregationClass, SUB_AGGREGATION, SUB_AGGREGATION_TYPE);
        } else if (SUB_AGGREGATION_TYPE_PIPELINE.isAssignableFrom(sub.getClass())) {

            optionalMethod = ReflectionUtils.findMethod(aggregationClass, SUB_AGGREGATION, SUB_AGGREGATION_TYPE_PIPELINE);
        }
        Optional<Method> finalOptionalMethod = optionalMethod;
        finalOptionalMethod.ifPresent(method -> {
            // 如果是管道聚合,不允许设置子聚合
            if (PipelineAggregationBuilder.class.isAssignableFrom(aggregationClass)) {
                throw new IllegalArgumentException("pipeline could not support aggregation!");
            }
            org.springframework.util.ReflectionUtils.invokeMethod(finalOptionalMethod.get(), root, sub);
        });
    }

    private void applyDateInterval(Object target, Class<?> aggregationClass, String name, Object intervalValue) {
        // 检测当前 aggregationClass 是否有此方法 (此方法的类型为 DateHistogramInterval)
        Optional<Method> optionalMethod = ReflectionUtils.findMethod(aggregationClass, name, DATE_INTERVAL_TYPE);
        optionalMethod.ifPresent(method -> {
            Object intervalObject = ReflectionUtils.getTargetInstanceViaReflection(DATE_INTERVAL_TYPE, intervalValue);
            org.springframework.util.ReflectionUtils.invokeMethod(optionalMethod.get(), target, intervalObject);
        });
    }


    private void applyDefaultField(Object target, Class<?> aggregationClass, Field subField, AggregationParameters parameters) {
        // 检测aggregationClass 是否拥有 subField这个方法
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

    private void applyScriptField(Object target, Class<?> aggregationClass, String name, Object scriptValue) {

        Optional<Method> optionalMethod = ReflectionUtils.findMethod(aggregationClass, name, SCRIPT_TYPE);
        optionalMethod.ifPresent(method -> {
            Object intervalObject = ReflectionUtils.getTargetInstanceViaReflection(SCRIPT_TYPE, scriptValue);
            org.springframework.util.ReflectionUtils.invokeMethod(optionalMethod.get(), target, intervalObject);
        });
    }

    public static void main(String[] args) {
        AggregationParameters parameters = new AggregationParameters();

        parameters.setName("customer_identity_card");
        parameters.setType("terms");
        GeneralParameters generalParameters = new GeneralParameters("customer_identity_card", 3);
        parameters.setGeneralParameters(generalParameters);

        AggregationParameters sub = new AggregationParameters();
        sub.setName("main_card_per");
        sub.setType("terms");
        GeneralParameters subGeneralParameters = new GeneralParameters("account_card", 4);
        sub.setGeneralParameters(subGeneralParameters);
        List<AggregationParameters> subList = new ArrayList<>();
        subList.add(sub);
        parameters.setSubAggregation(subList);
        AggregationMappingBuilder aggregationMappingBuilder = new AggregationMappingBuilder(
                new AggregationNameForBeanClassOfBucket()
        );

        Object target = aggregationMappingBuilder.buildAggregationInstance(parameters);
        System.out.println(target);


    }
}
