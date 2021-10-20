package com.zqykj.enums;

/**
 * @Description: metric aggregate type
 * @Author zhangkehou
 * @Date 2021/9/14
 */
public enum AggsType {

    /**
     * 求和
     */
    sum,

    /**
     * 最大值
     */
    max,

    /**
     * 最小值
     */
    min,

    /**
     * 平均值
     */
    avg,

    /**
     * 计数
     */
    count,

    /**
     * terms
     */
    terms,

    /**
     * bucket_script
     */
    bucket_script,

    /**
     * bucket_sort 聚合排序、分页
     */
    bucket_sort,

    /**
     * date histogram
     */
    date_histogram,

    /**
     * 过滤
     */
    filter,

    /**
     * top hits
     */
    top_hits
}
