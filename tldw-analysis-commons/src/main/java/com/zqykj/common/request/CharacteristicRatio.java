package com.zqykj.common.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 特征比
 * @Author zhangkehou
 * @Date 2021/10/26
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CharacteristicRatio {


    /**
     * 来源特征比参数.
     */
    private Integer source;

    /**
     * 中转特征比参数.
     */
    private Integer transfer;

    /**
     * 沉淀特征比参数.
     */
    private Integer precipitate;

}
