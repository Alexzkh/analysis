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
     * terms query(对于一个field , 可以指定多组value值)
     **/
    terms,

    /**
     * range
     */
    range,

    /**
     * bool query(组合查询)
     */
    bool;

    public static QueryType of(String type) {

        Objects.requireNonNull(type);

        return Stream.of(values())
                .filter(bean -> bean.toString().equals(type))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(type + " not exists!"));
    }
}
