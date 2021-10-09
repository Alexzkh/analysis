/**
 * @作者 Mcj
 */
package com.zqykj.core.aggregation.query.builder;

import com.zqykj.core.aggregation.AggregateRequestFactory;
import com.zqykj.parameters.aggregate.date.DateParams;
import com.zqykj.parameters.aggregate.CommonAggregationParams;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.Pagination;
import com.zqykj.parameters.aggregate.date.DateSpecificFormat;
import com.zqykj.parameters.aggregate.pipeline.PipelineAggregationParams;
import com.zqykj.core.aggregation.util.ClassNameForBeanClass;
import com.zqykj.core.aggregation.util.aggregate.bucket.ClassNameForBeanClassOfBucket;
import com.zqykj.core.aggregation.util.aggregate.metrics.ClassNameForBeanClassOfMetrics;
import com.zqykj.core.aggregation.util.aggregate.pipeline.ClassNameForBeanClassOfPipeline;
import com.zqykj.parameters.annotation.DateIntervalParameter;
import com.zqykj.parameters.annotation.OptionalParameter;
import com.zqykj.util.ReflectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.PipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.pipeline.BucketSortPipelineAggregationBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    private static Map<String, Class<?>> aggregateNameForClass = new ConcurrentReferenceHashMap<>(256);

    static {
        aggregateNameForClass.putAll(new ClassNameForBeanClassOfBucket().getAggregateNameForClass());
        aggregateNameForClass.putAll(new ClassNameForBeanClassOfMetrics().getAggregateNameForClass());
        aggregateNameForClass.putAll(new ClassNameForBeanClassOfPipeline().getAggregateNameForClass());
    }

    public AggregationMappingBuilder(ClassNameForBeanClass nameForBeanClass) {
        aggregateNameForClass = nameForBeanClass.getAggregateNameForClass();
    }

    public AggregationMappingBuilder(ClassNameForBeanClass... nameForBeanClass) {
        for (ClassNameForBeanClass forBeanClass : nameForBeanClass) {
            aggregateNameForClass.putAll(forBeanClass.getAggregateNameForClass());
        }
    }

    public static Object buildAggregation(AggregationParams parameters) {
        return AggregationMappingBuilder.buildAggregationInstance(null, parameters);
    }

    /**
     * <h2> 构建AggregationBuilder 聚合 </h2>
     *
     * @param father     第一次传递null
     * @param parameters 聚合参数
     */
    private static Object buildAggregationInstance(@Nullable Object father, AggregationParams parameters) {

        try {
            // TODO 聚合类型需要翻译 成对应数据源有的聚合类型
            parameters.setType(parameters.aggregationTypeConvert(parameters.getType()));

            // eg. max (对于es 来说有max, mongodb 可能不叫max)
            Class<?> aggregationClass = aggregateNameForClass.get(parameters.getType());
            //
            if (null == aggregationClass) {
                log.warn("could not find this aggregation type!");
                throw new IllegalArgumentException("could not find this aggregation type!");
            }

            // 看看此聚合类的构造函数是否可以填充 聚合名称
            Optional<Constructor<?>> constructor = ReflectionUtils.findConstructor(aggregationClass, parameters.getName());
            Object target;
            if (constructor.isPresent()) {

                target = ReflectionUtils.getTargetInstanceViaReflection(constructor, aggregationClass, parameters.getName());
            } else {
                target = buildSpecialAggregationConstructor(aggregationClass, parameters);
                if (null == target) {
                    throw new IllegalArgumentException("could not find match aggregation constructor!");
                }
            }
            return mapAggregation(father, target, parameters, aggregationClass);
        } catch (Exception e) {
            log.error("could not build aggregation, error msg = {}", e.getMessage());
            throw new ElasticsearchException("could not build aggregation", e);
        }
    }

    private static Object buildSpecialAggregationConstructor(Class<?> aggregationClass, AggregationParams parameters) {

        if (FilterAggregationBuilder.class.isAssignableFrom(aggregationClass)) {
            // 特殊处理这种过滤器聚合查询
            // 首先构建QueryBuilder
            Object queryTarget = QueryMappingBuilder.buildDslQueryBuilderMapping(parameters.getQuerySpecialParams());
            Optional<Constructor<?>> constructor = ReflectionUtils.findConstructor(aggregationClass, parameters.getName(), queryTarget);
            if (constructor.isPresent()) {
                return ReflectionUtils.getTargetInstanceViaReflection(constructor, aggregationClass, parameters.getName(), queryTarget);
            }
        }
        return null;
    }

    /**
     * <h2> 构建PipelineAggregationBuilder 聚合 </h2>
     */
    public static Object buildPipelineAggregationInstance(PipelineAggregationParams parameters) {

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
            if (StringUtils.isNotBlank(parameters.getBucketsPath())
                    && !CollectionUtils.isEmpty(parameters.getBucketsPathMap())) {
                log.error("Cannot use [bucketsPath] with [bucketsPaths] configuration option");
            }
            if (StringUtils.isBlank(parameters.getBucketsPath())
                    && CollectionUtils.isEmpty(parameters.getBucketsPathMap())) {
                log.error("Invalid parameters, must be non-null and non-empty, " +
                        "need [bucketsPath] or [bucketsPaths] configuration option.");
            }
            if (StringUtils.isBlank(parameters.getScript())) {

                constructor = ReflectionUtils.findConstructor(aggregationClass, parameters.getName(), parameters.getBucketsPath());
                if (constructor.isPresent()) {
                    target = ReflectionUtils.getTargetInstanceViaReflection(constructor, aggregationClass, parameters.getName(), parameters.getBucketsPath());
                } else {
                    // 特殊聚合类判断, 构造函数与大部分管道聚合不一致
                    return buildSpecialPipelineAggregation(aggregationClass, parameters);
                }
            } else {

                Script script = new Script(parameters.getScript());
                constructor = ReflectionUtils.findConstructor(aggregationClass, parameters.getName(), parameters.getBucketsPathMap(), script);
                if (constructor.isPresent()) {
                    target = ReflectionUtils.getTargetInstanceViaReflection(constructor, aggregationClass, parameters.getName(), parameters.getBucketsPathMap(), script);
                } else {
                    // TODO 可能存在一些不太统一的构造函数情况,等到发现的时候再去处理

                }
            }
            return target;
        } catch (Exception e) {
            log.error("could not build pipeline aggregation, error msg = {}", e.getMessage());
            throw new ElasticsearchException("could not build pipeline aggregation", e);
        }
    }

    private static Object buildSpecialPipelineAggregation(Class<?> aggregationClass, PipelineAggregationParams parameters) {

        Optional<Constructor<?>> constructor;
        Object target = null;
        // 管道聚合排序
        if (BucketSortPipelineAggregationBuilder.class.isAssignableFrom(aggregationClass)) {

            if (!CollectionUtils.isEmpty(parameters.getFieldSort())) {
                List<FieldSortBuilder> fieldSortBuilders = parameters.getFieldSort().stream().map(
                        fieldSort -> {
                            FieldSortBuilder fieldSortBuilder = new FieldSortBuilder(fieldSort.getFieldName());
                            fieldSortBuilder.order(SortOrder.fromString(fieldSort.getDirection().toString().toLowerCase()));
                            return fieldSortBuilder;
                        }
                ).collect(Collectors.toList());
                constructor = ReflectionUtils.findConstructor(aggregationClass, parameters.getName(), fieldSortBuilders);
                if (constructor.isPresent()) {
                    target = ReflectionUtils.getTargetInstanceViaReflection(aggregationClass, parameters.getName(), fieldSortBuilders);
                }
            } else {
                constructor = ReflectionUtils.findConstructor(aggregationClass, parameters.getName());
                if (constructor.isPresent()) {
                    target = ReflectionUtils.getTargetInstanceViaReflection(aggregationClass, parameters.getName());
                }
            }
            if (null != parameters.getPagination()) {
                // 设置 from, size
                addFromAndSize(target, aggregationClass, parameters.getPagination());
            }
        }
        return target;
    }

    private static void addFromAndSize(Object target, Class<?> aggregationClass, Pagination pagination) {

        org.springframework.util.ReflectionUtils.doWithFields(pagination.getClass(), field -> {

            Optional<Method> methodOptional = ReflectionUtils.findMethod(aggregationClass, field.getName(), field.getType());
            methodOptional.ifPresent(method -> {
                org.springframework.util.ReflectionUtils.makeAccessible(field);
                Object value = org.springframework.util.ReflectionUtils.getField(field, pagination);
                org.springframework.util.ReflectionUtils.invokeMethod(method, target, value);
            });
        });
    }


    private static Object mapAggregation(Object father, Object target, AggregationParams parameters, Class<?> aggregationClass) {

        // 处理 AggregationParameters 中不同类型的参数, 为 aggregation instance target 赋值
        org.springframework.util.ReflectionUtils.doWithFields(parameters.getClass(), field -> {

            try {
                buildingAggregationViaField(father, target, parameters, aggregationClass, field);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("error mapping property with name = {}", field.getName());
            }
        });

        return target;
    }


    private static void buildingAggregationViaField(Object father, Object target, AggregationParams parameters, Class<?> aggregationClass,
                                                    Field field) throws ClassNotFoundException {

        if (CommonAggregationParams.class.isAssignableFrom(field.getType())) {

            addGeneraParametersMapping(target, parameters.getCommonAggregationParams(), aggregationClass, field, field.getType());
        } else if (DateParams.class.isAssignableFrom(field.getType())) {

            addDateParametersMapping(target, parameters.getDateParams(), aggregationClass, field, field.getType());
        } else if (List.class.isAssignableFrom(field.getType())) {

            Optional<Class<?>> parameterType = ReflectionUtils.findParameterType(parameters.getClass(), field.getName());
            if (!parameterType.isPresent()) {
                return;
            }
            if (AggregationParams.class.isAssignableFrom(parameterType.get())) {

                // AggregationBuilder 类型子聚合处理
                addSubAggregationMapping(target, parameters.getSubAggregation(), aggregationClass);
            } else if (PipelineAggregationParams.class.isAssignableFrom(parameterType.get())) {

                // PipelineAggregationBuilder 类型聚合处理
                addPipelineAggregationMapping(father, parameters.getPipelineAggregation(), aggregationClass);
            }
        } else {
            addOptionalParameterMapping(target, aggregationClass, field, parameters);
        }
    }

    private static void addOptionalParameterMapping(Object target, Class<?> aggregationClass,
                                                    Field field, Object parameters) {

        if (field.isAnnotationPresent(OptionalParameter.class)) {
            applyDefaultField(target, aggregationClass, field, parameters);
        }
    }

    private static void addSubAggregationMapping(Object target, List<AggregationParams> subParameters, Class<?> aggregationClass) {

        if (CollectionUtils.isEmpty(subParameters)) {
            return;
        }
        for (AggregationParams subParameter : subParameters) {
            Object subTarget = buildAggregationInstance(target, subParameter);
            //
            applySubAggregation(target, aggregationClass, subTarget);
        }
    }

    private static void addPipelineAggregationMapping(Object target, List<PipelineAggregationParams> subParameters, Class<?> aggregationClass) {

        if (CollectionUtils.isEmpty(subParameters)) {
            return;
        }
        for (PipelineAggregationParams subParameter : subParameters) {
            Object subTarget = buildPipelineAggregationInstance(subParameter);
            //
            applySubAggregation(target, aggregationClass, subTarget);
        }
    }


    private static void addGeneraParametersMapping(Object target, CommonAggregationParams parameters,
                                                   Class<?> aggregationClass, Field field, Class<?> fieldClass) {

        if (null == parameters) {
            return;
        }
        // 处理GeneraParameters 中的每一个字段
        org.springframework.util.ReflectionUtils.doWithFields(fieldClass, subField -> {

            // 根据field name 默认处理 (AggregationParameters 定义的field name 都是匹配 aggregationClass 的方法名称)
            // 后面如果mongodb 方法名称根据当前es 的不一致, 中间加一层转换即可
            applyDefaultField(target, aggregationClass, subField, parameters);
        });
    }

    private static void addDateParametersMapping(Object target, DateParams parameters,
                                                 Class<?> aggregationClass, Field field, Class<?> fieldClass) {

        // Cannot use [fixed_interval] with [calendar_interval] configuration option.
        if (null == parameters) {
            return;
        }
        if (StringUtils.isNotBlank(parameters.getCalendarInterval()) && StringUtils.isNotBlank(parameters.getFixedInterval())) {

            if (log.isWarnEnabled()) {
                log.warn("Cannot use [fixedInterval] with [calendarInterval] configuration option.");
            }
            throw new IllegalArgumentException("Cannot use [fixedInterval] with [calendarInterval] configuration option.");
        } else if (StringUtils.isBlank(parameters.getCalendarInterval()) && StringUtils.isBlank(parameters.getFixedInterval())) {

            if (log.isWarnEnabled()) {
                log.warn("Invalid interval specified, must be non-null and non-empty");
            }
            throw new IllegalArgumentException("Invalid interval specified, must be non-null and non-empty, " +
                    "need [fixedInterval] or [calendarInterval] configuration option.");
        }
        // 处理GeneraParameters 中的每一个字段
        org.springframework.util.ReflectionUtils.doWithFields(fieldClass, subField -> {

            // 检测aggregationClass 是否拥有 subField这个方法
            // 日期类型的需要特殊处理
            // 目前先处理 date histogram 的相关参数
            if (subField.isAnnotationPresent(DateIntervalParameter.class)) {
                applyDateInterval(target, aggregationClass, subField, parameters);
            } else {
                // field 都正常按照名称处理
                applyDefaultField(target, aggregationClass, subField, parameters);
            }
        });
    }

    private static void applySubAggregation(Object root, Class<?> aggregationClass, Object sub) {
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

    private static void applyDateInterval(Object target, Class<?> aggregationClass, Field field, Object parameters) {
        // 检测当前 aggregationClass 是否有此方法 (此方法的类型为 DateHistogramInterval)
        Optional<Method> optionalMethod = ReflectionUtils.findMethod(aggregationClass, field.getName(), DATE_INTERVAL_TYPE);
        optionalMethod.ifPresent(method -> {
            org.springframework.util.ReflectionUtils.makeAccessible(field);
            Object intervalValue = org.springframework.util.ReflectionUtils.getField(field, parameters);
            if (null != intervalValue) {
                DateHistogramInterval value = new DateHistogramInterval(intervalValue.toString());
                org.springframework.util.ReflectionUtils.invokeMethod(optionalMethod.get(), target, value);
            }
        });
    }


    private static void applyDefaultField(Object target, Class<?> aggregationClass, Field subField, Object parameters) {
        // 检测aggregationClass 是否拥有 subField这个方法
        Optional<Method> optionalMethod = ReflectionUtils.findMethod(aggregationClass, subField.getName(), subField.getType());
        // 开始调用此聚合类的方法, 为target 赋值
        optionalMethod.ifPresent(method -> {
            org.springframework.util.ReflectionUtils.makeAccessible(subField);
            Object value = org.springframework.util.ReflectionUtils.getField(subField, parameters);
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

        // DateHistogram 例子
        AggregationParams dateGroupAndSum = AggregateRequestFactory.createDateGroupAndSum("trade_time",
                new DateSpecificFormat("1h", "HH"), "trade_amount");
        Object target = buildAggregation(dateGroupAndSum);
        System.out.println(target);
    }
}