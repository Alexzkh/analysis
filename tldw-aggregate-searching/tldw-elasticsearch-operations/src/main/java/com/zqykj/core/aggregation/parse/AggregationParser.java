/**
 * @作者 Mcj
 */
package com.zqykj.core.aggregation.parse;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zqykj.util.BigDecimalUtil;
import com.zqykj.util.JacksonUtils;
import org.elasticsearch.search.aggregations.Aggregations;

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
}
