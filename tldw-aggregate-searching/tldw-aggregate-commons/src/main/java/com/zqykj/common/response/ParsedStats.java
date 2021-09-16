package com.zqykj.common.response;


import java.util.HashMap;
import java.util.Map;

/**
 * @Description: TODO
 * @Author zhangkehou
 * @Date 2021/9/16
 */
public class ParsedStats implements Stats{


    private long count ;
    private double min;
    private double max;
    private double sum;
    private double avg;


    @Override
    public long getCount() {
        return count;
    }

    @Override
    public double getMin() {
        return min;
    }

    @Override
    public double getMax() {
        return max;
    }

    @Override
    public double getAvg() {
        return avg;
    }

    @Override
    public double getSum() {
        return sum;
    }

}
