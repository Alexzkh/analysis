/**
 * @作者 Mcj
 */
package com.zqykj.parameters.query;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 日期范围
 */
@Setter
@Getter
@NoArgsConstructor
public class DateRange {

    private Object from;

    private Object to;

    private String format = "yyyy-MM-dd HH:mm:ss";

    private String timeZone;

    // 小于
    private Object lt;

    // 小于等于
    private Object lte;

    // 大于
    private Object gt;

    // 大于等于
    private Object gte;

    public DateRange(Object from, Object to) {
        this.from = from;
        this.to = to;
    }

    public DateRange(Object from) {
        this.from = from;
    }

    /**
     * <h2> >= and <= </h2>
     */
    public static DateRange gteLte(Object from, Object to) {
        DateRange dateRange = new DateRange();
        dateRange.setGte(from);
        dateRange.setLte(to);
        return dateRange;
    }

    /**
     * <h2> > and < </h2>
     */
    public static DateRange gtLt(Object from, Object to) {
        DateRange dateRange = new DateRange();
        dateRange.setGt(from);
        dateRange.setLt(to);
        return dateRange;
    }

    /**
     * <h2> < </h2>
     */
    public static DateRange lt(Object to) {
        DateRange dateRange = new DateRange();
        dateRange.setLt(to);
        return dateRange;
    }

    /**
     * <h2> <= </h2>
     */
    public static DateRange lte(Object to) {
        DateRange dateRange = new DateRange();
        dateRange.setLte(to);
        return dateRange;
    }

    /**
     * <h2> >= </h2>
     */
    public static DateRange gte(Object from) {
        DateRange dateRange = new DateRange();
        dateRange.setGte(from);
        return dateRange;
    }

    /**
     * <h2> > </h2>
     */
    public static DateRange gt(Object from) {
        DateRange dateRange = new DateRange();
        dateRange.setGt(from);
        return dateRange;
    }

    /**
     * <h2> between 包含边界 </h2>
     */
    public static DateRange between(Object from, Object to, boolean isIncludeBound) {
        if (isIncludeBound) {
            return new DateRange(from, to);
        }
        DateRange dateRange = new DateRange();
        dateRange.setGt(from);
        dateRange.setLt(to);
        return dateRange;
    }
}
