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
     * group by多字段
     */
    multiTerms,

    /**
     * bucket_script
     */
    bucket_script,

    /**
     * bucket_sort 聚合排序、分页
     */
    bucket_sort,

    /**
     * 根据分组后的值筛选
     */
    bucket_selector,

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
    top_hits,


    /**
     * 去重
     */
    cardinality,

    /**
     * 管道聚合--求和
     */
    sum_bucket
}
