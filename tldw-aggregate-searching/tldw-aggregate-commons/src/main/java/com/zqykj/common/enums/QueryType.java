package com.zqykj.common.enums;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * @Description: 聚合之外的查询类型
 * @Author zhangkehou
 * @Date 2021/9/24
 */
public enum QueryType {

    /**
     * term query
     **/
    term,

    /**
     * range
     * */
    range,

    /**
     * bool query(组合查询)
     */
    bool,

    /**
     * 类似于 and
     */
    must,

    /**
     * 类似于 !=
     */
    must_not,

    /**
     * 类似于 or 查询条件
     */
    should,

    /**
     * 过滤
     */
    filter;

    public static QueryType of(String type) {

        Objects.requireNonNull(type);

        return Stream.of(values())
                .filter(bean -> bean.toString().equals(type))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(type + " not exists!"));
    }
}
