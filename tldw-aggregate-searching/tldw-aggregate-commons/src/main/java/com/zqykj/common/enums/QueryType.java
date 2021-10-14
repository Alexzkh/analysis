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
     */
    range,

    /**
     * bool query(组合查询)
     */
    bool,

    /**
     * 多值查询
     */
    terms,

    /**
     * 通配符查询
     */
    wildcard;

    public static QueryType of(String type) {

        Objects.requireNonNull(type);

        return Stream.of(values())
                .filter(bean -> bean.toString().equals(type))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(type + " not exists!"));
    }
}
