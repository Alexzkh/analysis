/**
 * @作者 Mcj
 */
package com.zqykj.parameters.aggregate.date;

import com.zqykj.parameters.annotation.DateIntervalParameter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;

/**
 * <h1> Date 聚合参数 </h1>
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DateParams {

    /**
     * 仅仅支持单个日历单位 eg. 1h(代表间隔一小时) , 这里不能超过1, eg. 2h(就会报错), 如果需要固定间隔,请使用fixedInterval(这里就可以输入2h等等)
     * 支持的单位 s(秒),m(分钟),h(小时),d(天),w(周),M(月),y(年)
     */
    @DateIntervalParameter
    private String calendarInterval;

    /**
     * 日期格式 eg. yyyy-MM-dd HH:mm:ss 等等
     */
    private String format;

    /**
     * 固定日历间隔参数配置 (支持的单位: s(秒)、m(分钟)、h(小时)、d(天))
     * 如果 使用了此参数, calendarInterval 这个参数就不能赋值, 2者不能同时存在
     */
    @DateIntervalParameter
    private String fixedInterval;

    /**
     * buckets 内 doc_count 的数量限制, 只有bucket 内的doc_count 大于等于这个minDocCount 才会被保留
     */
    private long minDocCount;

    /**
     * 需要忽略的
     */
    private Object missing;

    public DateParams(String calendarInterval, String format) {
        this.calendarInterval = calendarInterval;
        this.format = format;
    }

    public DateParams(String fixedInterval, String format, @Nullable String calendarInterval) {
        this.fixedInterval = fixedInterval;
        this.format = format;
    }

    public DateParams(String calendarInterval, String format, long minDocCount) {
        this.calendarInterval = calendarInterval;
        this.format = format;
        this.minDocCount = minDocCount;
    }

    public DateParams(String calendarInterval, String format, Object missing) {
        this.calendarInterval = calendarInterval;
        this.format = format;
        this.missing = missing;
    }

    public DateParams(String calendarInterval, String format, long minDocCount, Object missing) {
        this.calendarInterval = calendarInterval;
        this.format = format;
        this.minDocCount = minDocCount;
        this.missing = missing;
    }

}
