package com.zqykj.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Description: 特征比枚举类
 * @Author zhangkehou
 * @Date 2021/12/27
 */
@Getter
@AllArgsConstructor
public enum CharacteristicRatioType {

    SOURCE(1, "来源"),

    TRANSFER(2, "中转"),

    PRECIPITATE(3, "沉淀"),

    OTHER(4, "其他");

    // 特征比编码
    private Integer code;

    // 特征比类型
    private String type;

}
