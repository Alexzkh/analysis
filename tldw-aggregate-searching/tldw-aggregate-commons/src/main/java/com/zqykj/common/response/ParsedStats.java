package com.zqykj.common.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @Description: Convert elasticsearch return data
 * @Author zhangkehou
 * @Date 2021/9/18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParsedStats {


    /**
     * statistics
     */
    private long count;

    /**
     * minimum value
     */
    private double min;

    /**
     * maximum value
     */
    private double max;

    /**
     * sum
     */
    private double sum;

    /**
     * average value
     */
    private double avg;

    public static ParsedStats build(org.elasticsearch.search.aggregations.metrics.ParsedStats parsedStats) {
        return ParsedStats.builder()
                .avg(parsedStats.getAvg())
                .sum(parsedStats.getSum())
                .min(parsedStats.getMin())
                .count(parsedStats.getCount())
                .max(parsedStats.getMax())
                .build();

    }
}
