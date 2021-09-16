package com.zqykj.common.request;


/**
 * @Description: The interval the date histogram is based on.
 * @Author zhangkehou
 * @Date 2021/9/16
 */
public class DateHistogramParams {

    public static final DateHistogramParams SECOND = new DateHistogramParams("1s");
    public static final DateHistogramParams MINUTE = new DateHistogramParams("1m");
    public static final DateHistogramParams HOUR = new DateHistogramParams("1h");
    public static final DateHistogramParams DAY = new DateHistogramParams("1d");
    public static final DateHistogramParams WEEK = new DateHistogramParams("1w");
    public static final DateHistogramParams MONTH = new DateHistogramParams("1M");
    public static final DateHistogramParams QUARTER = new DateHistogramParams("1q");
    public static final DateHistogramParams YEAR = new DateHistogramParams("1y");
    private final String expression;

    public DateHistogramParams(String expression) {
        this.expression = expression;
    }

    public static DateHistogramParams seconds(int sec) {
        return new DateHistogramParams(sec + "s");
    }

    public static DateHistogramParams minutes(int min) {
        return new DateHistogramParams(min + "m");
    }

    public static DateHistogramParams hours(int hours) {
        return new DateHistogramParams(hours + "h");
    }

    public static DateHistogramParams days(int days) {
        return new DateHistogramParams(days + "d");
    }

    public static DateHistogramParams weeks(int weeks) {
        return new DateHistogramParams(weeks + "w");
    }
}
