/**
 * @作者 Mcj
 */
package com.zqykj.core.aggregation.query.parameters.aggregate;

import lombok.*;

/**
 * <h1> Date 聚合参数 </h1>
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DateParameters {

    /**
     * 仅仅支持单个日历单位 eg. 1d(间隔一天, 如果定义2d) (支持的单位: 秒、分钟、小时、天、周、月、年)
     */
    private String calendarInterval;

    /**
     * 日期格式 eg. yyyy-MM-dd HH:mm:ss 等等
     */
    private String format;

    /**
     * 固定日历间隔参数配置 (支持的单位: 毫秒、秒、分钟、小时、天)
     */
    private String fixedInterval;

    public DateParameters(String calendarInterval, String format) {
        this.calendarInterval = calendarInterval;
        this.format = format;
    }

}
