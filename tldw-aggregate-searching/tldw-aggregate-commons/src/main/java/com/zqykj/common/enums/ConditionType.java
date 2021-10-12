/**
 * @作者 Mcj
 */
package com.zqykj.common.enums;

import java.util.Objects;
import java.util.stream.Stream;

public enum ConditionType {

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

    public static ConditionType of(String type) {

        Objects.requireNonNull(type);

        return Stream.of(values())
                .filter(bean -> bean.toString().equals(type))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(type + " not exists!"));
    }
}
