package com.zqykj.common.response;


import lombok.Builder;
import lombok.Data;


/**
 * @Description: Convert elasticsearch return data
 * @Author zhangkehou
 * @Date 2021/9/18
 */
@Data
@Builder
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
}
