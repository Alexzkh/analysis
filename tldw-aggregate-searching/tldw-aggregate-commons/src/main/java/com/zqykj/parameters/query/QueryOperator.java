/**
 * @作者 Mcj
 */
package com.zqykj.parameters.query;


import java.util.Objects;
import java.util.stream.Stream;

/**
 * 查询比较符
 */
public enum QueryOperator {

    eq, gt, gte, lt, lte;

    public static QueryOperator of(String type) {

        Objects.requireNonNull(type);

        return Stream.of(values())
                .filter(bean -> bean.toString().equals(type))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(type + " not exists!"));
    }
}
