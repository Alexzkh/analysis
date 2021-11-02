/**
 * @作者 Mcj
 */
package com.zqykj.core.aggregation.factory;

import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.aggregate.date.DateParams;
import com.zqykj.parameters.aggregate.date.DateSpecificFormat;
import com.zqykj.parameters.aggregate.pipeline.PipelineAggregationParams;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * <h1> 内部聚合查询请求参数 构建factory</h1>
 */
public class AggregateRequestFactory {


    /**
     * <h2> 按日期field 和 固定日期间隔 分组 且 对某个字段 sum </h2>
     * <p> 聚合名称按规律自动填充  聚合类型_聚合字段 </p>
     */
    public static AggregationParams createDateGroupAndSum(String dateField, DateSpecificFormat specificFormat,
                                                          String sumField) {

        // 这里可以自定义聚合名称的拼接方式
        String dateAggregateName = "date_histogram_" + dateField;
        String sumAggregateName = "sum_" + sumField;
        DateParams dateParams = new DateParams();
        dateParams.setFormat(specificFormat.getFormat());
        // default
        dateParams.setMinDocCount(1);
        if (StringUtils.isNotBlank(specificFormat.getFixedInterval()) && StringUtils.isNotBlank(specificFormat.getCalendarInterval())) {

            throw new IllegalArgumentException("Cannot use [fixedInterval] with [calendarInterval] configuration option.");
        } else if (StringUtils.isNotBlank(specificFormat.getFixedInterval())) {

            dateParams.setFixedInterval(specificFormat.getFixedInterval());
        } else {

            dateParams.setCalendarInterval(specificFormat.getCalendarInterval());
        }
        AggregationParams root = new AggregationParams(dateAggregateName, "date_histogram", dateField, dateParams);

        AggregationParams sub = new AggregationParams(sumAggregateName, "sum", sumField);

        root.setPerSubAggregation(sub);

        Map<String, String> bucketsPathMap = new HashMap<>();
        bucketsPathMap.put("final_sum", sumAggregateName);
        PipelineAggregationParams pipelineAggregationParams =
                new PipelineAggregationParams("sum_bucket_selector", "bucket_selector",
                        bucketsPathMap, "params.final_sum > 0");
        sub.setPerSubAggregation(pipelineAggregationParams);
        return root;
    }


    public static DateSpecificFormat convertFromTimeType(String timeType) {

        switch (timeType) {
            case "d":
                return new DateSpecificFormat("1d", "yyyy-MM-dd");
            case "M":
                return new DateSpecificFormat("1M", "yyyy-MM");
            case "y":
                return new DateSpecificFormat("1y", "yyyy");
            case "q":
                return new DateSpecificFormat("1q", "yyyy-MM");
            default:
                return new DateSpecificFormat("1h", "HH");
        }
    }

}
