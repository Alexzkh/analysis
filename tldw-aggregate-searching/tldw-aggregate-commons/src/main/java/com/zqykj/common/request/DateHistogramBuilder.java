package com.zqykj.common.request;

import com.zqykj.enums.AggsType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description: TODO
 * @Author zhangkehou
 * @Date 2021/9/28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DateHistogramBuilder {

    /**
     * 聚合类型
     * */
    private AggsType aggsType;

    /**
     * 聚合字段
     * */
    private String field;

    /**
     * 格式化
     * */
    private String format;

    /**
     * 桶的最小doc数目
     * */
    private long minDocCount;

    /**
     * 日期间隔单位
     * */
    private String dateIntervalUnit;

    /**
     * 子聚合
     * */
    private List<DateHistogramBuilder> childDateHistogramBuilders;


}
