/**
 * @作者 Mcj
 */
package com.zqykj.core.aggregation.parse;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zqykj.util.BigDecimalUtil;
import com.zqykj.util.JacksonUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.histogram.ParsedDateHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.*;

/**
 * <h1> 聚合结果解析器 </h1>
 */
public class AggregationParser {

    private final static String BUCKET_KEY = "buckets";
    private final static String KEY_AS_STRING = "keyAsString";
    private final static String VALUE_AS_STRING = "valueAsString";
    private final static String AGGREGATIONS_KEY = "aggregations";
    private final static String AS_MAP_KEY = "asMap";
    private final static String AGG_NAME = "name";
    private final static String SUB_AGGREGATION = "aggregations";
    private final static String AGG_TYPE = "type";
    private final static String SHOW_FIELD_AGG = "top_hits";
    private final static String AGG_HITS = "hits";
    private final static String AGG_SOURCE_STRING = "sourceAsString";
    private final static String AGG_TERMS = "terms";
    private final static String AGG_METHOD_PREFIX = "get";
    private final static String AGG_DATE_HISTOGRAM = "date_histogram";

    public static Map<String, Object> parseDateGroupAndSum(Aggregations aggregations) {


        String json = JacksonUtils.toJson(aggregations);
        // 首先如果是buckets 的话, 需要逐个处理
        Map<String, Object> parse = JacksonUtils.parse(json, new TypeReference<Map<String, Object>>() {
        });

        Map<String, Object> result = new LinkedHashMap<>();

        parse.forEach((key, value) -> {

            System.out.println(key);
            System.out.println(value);

            if (value instanceof LinkedHashMap) {

                Collection<Object> collection = ((LinkedHashMap) value).values();

                collection.forEach(value1 -> {

                    if (value1 instanceof LinkedHashMap) {

                        LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) value1;

                        map.forEach((key2, value2) -> {

                            if (BUCKET_KEY.equals(key2)) {

                                // buckets
                                if (value2 instanceof ArrayList) {

                                    ArrayList<Object> buckets = (ArrayList<Object>) value2;

                                    buckets.forEach(bucket -> {

                                        if (bucket instanceof LinkedHashMap) {

                                            LinkedHashMap<String, Object> bucketMap = (LinkedHashMap<String, Object>) bucket;
                                            String keyAsString = null;
                                            String valueAsString = null;
                                            for (Map.Entry<String, Object> entry : bucketMap.entrySet()) {
                                                String key3 = entry.getKey();
                                                Object value3 = entry.getValue();
                                                if (KEY_AS_STRING.equals(key3)) {
                                                    keyAsString = value3.toString();
                                                } else if (AGGREGATIONS_KEY.equals(key3)) {

                                                    // 子聚合, 这里的话可以递归处理子聚合
                                                    LinkedHashMap<String, Object> asMap = ((LinkedHashMap) value3);
                                                    LinkedHashMap<String, Object> aggregationsMap = (LinkedHashMap<String, Object>) asMap.get(AS_MAP_KEY);

                                                    Collection<Object> values = aggregationsMap.values();
                                                    for (Object value4 : values) {
                                                        if (value4 instanceof LinkedHashMap) {

                                                            LinkedHashMap<String, Object> map1 = (LinkedHashMap) value4;
                                                            valueAsString = map1.get(VALUE_AS_STRING).toString();
                                                        }
                                                    }
                                                }
                                            }
                                            if (result.containsKey(keyAsString)) {

                                                String oldValue = result.get(keyAsString).toString();
                                                result.put(keyAsString, BigDecimalUtil.add(oldValue, valueAsString));
                                            } else {
                                                result.put(keyAsString, valueAsString);
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }
                });
            }
        });
//        result.entrySet().stream()
//                .sorted(Map.Entry.comparingByKey())
//                .forEachOrdered(x -> result.put(x.getKey(), x.getValue()));
        return result;
    }

    /**
     * <h2> 解析多组聚合结果 </h2>
     */
    public static List<List<Object>> parseMulti(Aggregations aggregations, Map<String, String> map) {

        // 讲首字母大写然后拼接上 get

        List<List<Object>> result = new ArrayList<>();
        map.forEach((key, value) -> {

            List<Object> parse = parse(aggregations, value, key);

            result.add(parse);
        });

        // 将不同列的属性值放到一组list中
        return convertFromResult(result);
    }

    private static List<List<Object>> convertFromResult(List<List<Object>> result) {

        if (CollectionUtils.isEmpty(result)) {
            return result;
        }

        List<List<Object>> newResult = new ArrayList<>();

        int size = result.get(0).size();

        for (int i = 0; i < size; i++) {

            List<Object> newOne = new ArrayList<>();

            for (List<Object> oldOne : result) {

                newOne.add(oldOne.get(i));
            }
            newResult.add(newOne);
        }
        return newResult;
    }

    /**
     * <h2> 解析聚合结果 </h2>
     */
    public static List<Object> parse(Aggregations aggregations, String key, String aggregationName) {

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

        Method[] methods = aClass.getMethods();

        if (methods.length == 0) {
            return;
        }

        String type = aggregation.getType();

        if (type.endsWith(AGG_TERMS) || type.endsWith(AGG_DATE_HISTOGRAM)) {


            // 桶聚合
            Optional<Method> methodOptional = Arrays.stream(methods).filter(method ->
                    method.getName().endsWith(applyFirstChartUpperCase(BUCKET_KEY))).findFirst();
            methodOptional.ifPresent(method -> parseBucketAggregation(result, aClass, aggregation,
                    aggMethodKey, aggregationName));


        } else if (aggregation.getName().equals(aggregationName) && !type.endsWith(AGG_TERMS)) {
            Optional<Method> optionalMethod = getAggregationMethod(aggMethodKey, methods);

            if (optionalMethod.isPresent()) {

                Object value = ReflectionUtils.invokeMethod(optionalMethod.get(), aggregation);

                result.add(value);
            }
        } else {
            // 子聚合处理
            Optional<Method> methodOptional = Arrays.stream(methods).filter(method ->
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
    private static void parseBucketAggregation(List<Object> result, Class<? extends Aggregation> clazz, Aggregation aggregation,
                                               String aggMethodKey, String aggregationName) {

        Optional<Method> methodOptional = com.zqykj.util.ReflectionUtils.findMethod(clazz, applyFirstChartUpperCase(BUCKET_KEY));

        if (methodOptional.isPresent()) {

            Object value = ReflectionUtils.invokeMethod(methodOptional.get(), aggregation);

            if (null != value  ) {

                List<? extends MultiBucketsAggregation.Bucket> buckets = (List<? extends MultiBucketsAggregation.Bucket>) value;

                for (MultiBucketsAggregation.Bucket bucket : buckets) {

                    Class<? extends MultiBucketsAggregation.Bucket> aClass = bucket.getClass();
                    Method[] methods = aClass.getMethods();
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

//    private static void getResultBy

    private static Optional<Method> getAggregationMethod(String aggMethodKey, Method[] methods) {
        return Arrays.stream(methods).filter(method -> method.getName().equals(aggMethodKey)).findFirst();
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

}
