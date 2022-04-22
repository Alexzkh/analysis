/**
 * @作者 Mcj
 */
package com.zqykj.core.aggregation.parse;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation.Bucket;

import org.elasticsearch.search.aggregations.pipeline.ParsedSimpleValue;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.*;

/**
 * <h1> 聚合结果解析器 </h1>
 */
@Slf4j
public class AggregationParser {

    private final static String BUCKET_KEY = "buckets";
    private final static String SUB_AGGREGATION = "aggregations";
    private final static String AGG_TERMS = "terms";
    private final static String AGG_METHOD_PREFIX = "get";
    private final static String AGG_DATE_HISTOGRAM = "date_histogram";
    private final static String GET_VALUE_METHOD = "getValue";
    private final static String VALUE_METHOD = "value";

    /**
     * <h2> 解析多组聚合结果 </h2>
     * <p>
     * TODO 后续将继续优化此取值算法
     */
    public static List<List<Object>> parseMulti(Aggregations aggregations, Map<String, String> map, boolean isConvert) {

        // 将首字母大写然后拼接上 get

        List<List<Object>> result = new ArrayList<>();
        map.forEach((key, value) -> {

            List<Object> parse = parse(aggregations, key, value);
            // 不处理空结果
            if (CollectionUtils.isEmpty(parse)) {

                if (log.isDebugEnabled()) {
                    log.debug("聚合名称 = {} , 取值属性 = {}, 取值结果个数 = {}", key, value, 0);
                }
            }
            result.add(parse);
        });

        // 将不同列的属性值放到一组list中
        if (isConvert) {
            return convertFromResult(result);
        }
        return result;
    }

    private static List<List<Object>> convertFromResult(List<List<Object>> result) {

        if (CollectionUtils.isEmpty(result)) {
            return result;
        }

        List<List<Object>> newResult = new ArrayList<>();

        // 筛选出size最大的
        int size = result.stream().max((Comparator.comparingInt(List::size))).orElse(new ArrayList<>()).size();

        if (size == 0) {
            return new ArrayList<>();
        }
        // 汇聚每一行的列值
        for (int i = 0; i < size; i++) {

            List<Object> newOne = new ArrayList<>();

            for (List<Object> oldOne : result) {

                if (oldOne.size() == size) {
                    Object o = oldOne.get(i);
                    if (o instanceof SearchHits) {
                        List<Map<String, Object>> source = new ArrayList<>();
                        SearchHits hits = (SearchHits) o;
                        SearchHit[] searchHits = hits.getHits();
                        for (SearchHit searchHit : searchHits) {
                            source.add(searchHit.getSourceAsMap());
                        }
                        newOne.add(source);
                    } else {
                        newOne.add(oldOne.get(i));
                    }
                } else {
                    // 填充的个数跟 size 一致 (因为有的跟terms 同级查询的,比如去重返回结果只有一个,而terms有3个,为了保证这里程序正确,
                    // 需要继续填充剩下2个冗余的数据(跟第一个数据一致)
                    // 若newOne 为空,自动补充一个
                    if (CollectionUtils.isEmpty(oldOne)) {
                        newOne.add(null);
                    } else {
                        newOne.add(oldOne.get(0));
                    }
                }
            }
            newResult.add(newOne);
        }
        //
        return newResult;
    }

    /**
     * <h2> 解析聚合结果 </h2>
     */
    public static List<Object> parse(Aggregations aggregations, String aggregationName, String key) {

        // 支持下划线驼峰处理
        key = applyCamelCase(key);
        // 讲首字母大写然后拼接上 get
        key = applyFirstChartUpperCase(key);

        ArrayList<Object> result = new ArrayList<>();
        standardParse(result, aggregations, key, aggregationName);
        return result;
    }

    public static void standardParse(List<Object> result, Aggregations aggregations, String key, String aggregationName) {

        List<Aggregation> list = aggregations.asList();

        for (Aggregation agg : list) {

            parseAggregationClass(result, agg, key, aggregationName);
        }
    }

    /**
     * <h2> 解析聚合类 </h2>
     */
    private static void parseAggregationClass(List<Object> result, Aggregation aggregation, String aggMethodKey, String aggregationName) {

        Class<? extends Aggregation> aClass = aggregation.getClass();

        List<Method> methods = com.zqykj.util.ReflectionUtils.getAllMethods(aClass);

        if (CollectionUtils.isEmpty(methods)) {
            return;
        }

        String type = aggregation.getType();

        if (type.endsWith(AGG_TERMS) || type.endsWith(AGG_DATE_HISTOGRAM)) {


            // 桶聚合
            Optional<Method> methodOptional = methods.stream().filter(method ->
                    method.getName().endsWith(applyFirstChartUpperCase(BUCKET_KEY))).findFirst();
            methodOptional.ifPresent(method -> parseBucketAggregation(result, aClass, (MultiBucketsAggregation) aggregation,
                    aggMethodKey, aggregationName));


        } else if (aggregation.getName().equals(aggregationName) && !type.endsWith(AGG_TERMS)) {
            String newKey = aggMethodKey;
            // ParsedSimpleValue 只有 getValueAsString() 方法,没有getValue方法,而是value() 方法. 由于一开始我们给 aggMethodKey 统一加上了get前缀
            // 导致调用的方法不存在
            if (ParsedSimpleValue.class.isAssignableFrom(aClass) && aggMethodKey.equals(GET_VALUE_METHOD)) {
                newKey = VALUE_METHOD;
            }
            Optional<Method> optionalMethod = getAggregationMethod(newKey, methods);

            if (optionalMethod.isPresent()) {

                Object value = ReflectionUtils.invokeMethod(optionalMethod.get(), aggregation);

                result.add(value);
            }
        } else {
            // 子聚合处理
            Optional<Method> methodOptional = methods.stream().filter(method ->
                    method.getName().equals(applyFirstChartUpperCase(SUB_AGGREGATION))).findFirst();
            methodOptional.ifPresent(method -> {

                Object o = ReflectionUtils.invokeMethod(method, aggregation);
                if (o == null) {
                    return;
                }
                if (o instanceof Aggregations) {

                    Aggregations aggregations = (Aggregations) o;
                    parseSubAggregations(aggregations, result, aggMethodKey, aggregationName);
                }
            });
        }
    }

    /**
     * <h2> 解析桶聚合 </h2>
     */
    private static void parseBucketAggregation(List<Object> result, Class<? extends Aggregation> clazz, MultiBucketsAggregation aggregation,
                                               String aggMethodKey, String aggregationName) {

        Optional<Method> methodOptional = com.zqykj.util.ReflectionUtils.findMethod(clazz, applyFirstChartUpperCase(BUCKET_KEY));

        if (methodOptional.isPresent()) {

            Object value = ReflectionUtils.invokeMethod(methodOptional.get(), aggregation);

            if (null != value) {
                List<? extends Bucket> buckets = (List<? extends Bucket>) value;
                for (Bucket bucket : buckets) {
                    Class<? extends Bucket> aClass = bucket.getClass();
                    List<Method> methods = com.zqykj.util.ReflectionUtils.getAllMethods(aClass);
                    Aggregations aggregations = bucket.getAggregations();
                    // 首先找这一层的相关属性, 找不到看看是否有子聚合(继续下钻处理)
                    if (aggregation.getName().equals(aggregationName)) {

                        Optional<Method> optionalMethod = getAggregationMethod(aggMethodKey, methods);

                        if (optionalMethod.isPresent()) {

                            Object o = ReflectionUtils.invokeMethod(optionalMethod.get(), bucket);
                            result.add(o);
                        } else {

                            // 去子聚合里面去找
                            if (null != aggregations) {
                                parseSubAggregations(aggregations, result, aggMethodKey, aggregationName);
                            }
                        }
                    } else {
                        // 去子聚合里面去找
                        if (null != aggregations) {
                            parseSubAggregations(aggregations, result, aggMethodKey, aggregationName);
                        }
                    }
                }
            }
        }
    }

    private static Optional<Method> getAggregationMethod(String aggMethodKey, List<Method> methods) {
        return methods.stream().filter(method -> method.getName().equals(aggMethodKey)).findFirst();
    }

    /**
     * <h2> 解析子聚合 </h2>
     */
    private static void parseSubAggregations(Aggregations aggregations, List<Object> result, String aggMethodKey, String aggregationName) {

        standardParse(result, aggregations, aggMethodKey, aggregationName);
    }


    private static String applyFirstChartUpperCase(String key) {

        return AGG_METHOD_PREFIX + key.substring(0, 1).toUpperCase() + key.substring(1);
    }

    /**
     * <h2> 将下划线转成驼峰 </h2>
     */
    private static String applyCamelCase(String underscoreStr) {
        String[] split = StringUtils.split(underscoreStr, "_");
        if (null == split || split.length == 0) {
            return underscoreStr;
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < split.length; i++) {
            String str = split[i];
            if (i == 0) {
                builder.append(str);
            } else {
                // 首字母大写
                String toUpper = str.substring(0, 1).toUpperCase() + str.substring(1);
                builder.append(toUpper);
            }
        }
        return builder.toString();
    }
}
