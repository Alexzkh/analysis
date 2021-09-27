package com.zqykj.repository.util;

import com.zqykj.enums.AggsType;
import com.zqykj.enums.DateIntervalUnit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.support.ValuesSourceAggregationBuilder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @Description: 日期直方图和mertrics数据聚合操作工具类
 * @Author zhangkehou
 * @Date 2021/9/27
 */
public class DateHistogramIntervalUtil {


    public static Map<AggsType, BiFunction<String, String, ValuesSourceAggregationBuilder>> map = new ConcurrentHashMap<>();

    /**
     * metrics aggregation builders
     * */
    static {
        map.put(AggsType.count, (type, field) -> AggregationBuilders.count(type).field(field));
        map.put(AggsType.avg, (type, field) -> AggregationBuilders.avg(type).field(field));
        map.put(AggsType.max, (type, field) -> AggregationBuilders.max(type).field(field));
        map.put(AggsType.min, (type, field) -> AggregationBuilders.min(type).field(field));
        map.put(AggsType.sum, (type, field) -> AggregationBuilders.sum(type).field(field));
        map.put(AggsType.terms, (type, field) -> AggregationBuilders.terms(type).field(field));

    }

    public static Map<DateIntervalUnit, Function<Integer, DateHistogramInterval>> dateHistogramInterval = new ConcurrentHashMap<>();

    /**
     * date histogram interval aggregation builders
     * */
    static {
        dateHistogramInterval.put(DateIntervalUnit.SECOND, sec -> DateHistogramInterval.seconds(sec));
        dateHistogramInterval.put(DateIntervalUnit.DAY, sec -> DateHistogramInterval.days(sec));
        dateHistogramInterval.put(DateIntervalUnit.MINUTE, minutes -> DateHistogramInterval.minutes(minutes));
        dateHistogramInterval.put(DateIntervalUnit.HOUR, hours -> DateHistogramInterval.hours(hours));
        dateHistogramInterval.put(DateIntervalUnit.WEEK, week -> DateHistogramInterval.weeks(week));
        dateHistogramInterval.put(DateIntervalUnit.QUARTER, quarter -> DateHistogramInterval.QUARTER);
        dateHistogramInterval.put(DateIntervalUnit.MONTH, month -> DateHistogramInterval.MONTH);
        dateHistogramInterval.put(DateIntervalUnit.YEAR, year -> DateHistogramInterval.YEAR);
    }

}
