package com.zqykj.common.response;

/**
 * @Description: TODO
 * @Author zhangkehou
 * @Date 2021/9/16
 */
public interface Stats {

    /**
     * @return The number of values that were aggregated.
     */
    long getCount();

    /**
     * @return The minimum value of all aggregated values.
     */
    double getMin();

    /**
     * @return The maximum value of all aggregated values.
     */
    double getMax();

    /**
     * @return The avg value over all aggregated values.
     */
    double getAvg();

    /**
     * @return The sum of aggregated values.
     */
    double getSum();

}
