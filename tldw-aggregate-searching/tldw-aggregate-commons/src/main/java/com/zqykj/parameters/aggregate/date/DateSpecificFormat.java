/**
 * @作者 Mcj
 */
package com.zqykj.parameters.aggregate.date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * <h1> 日期特有格式 </h1>
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DateSpecificFormat {

    /**
     * 仅仅支持单个日历单位 eg. 1h(代表间隔一小时) , 这里不能超过1, eg. 2h(就会报错), 如果需要固定间隔,请使用fixedInterval(这里就可以输入2h等等)
     * 支持的单位 s(秒),m(分钟),h(小时),d(天),w(周),M(月),y(年)
     */
    private String calendarInterval;

    /**
     * 日期格式 eg. yyyy-MM-dd HH:mm:ss 等等
     */
    private String format;

    /**
     * 固定日历间隔参数配置 (支持的单位: s(秒)、m(分钟)、h(小时)、d(天))  eg.  2s, 2m, 2h ....
     * 如果 使用了此参数, calendarInterval 这个参数就不能赋值, 2者不能同时存在
     */
    private String fixedInterval;

    public DateSpecificFormat(String calendarInterval, String format) {

        this.calendarInterval = calendarInterval;
        this.format = format;
    }
}
