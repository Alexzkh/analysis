package com.zqykj.app.service.transform;

import com.zqykj.common.request.CharacteristicRatio;

import java.util.function.BiFunction;

/**
 * @Description: 主要解析来源特征比、中转特征比、沉淀特征比
 * @Author zhangkehou
 * @Date 2021/10/26
 */
public final class CharacteristicRatioConversion {

    /**
     * 计算特征比类型.
     */
    private static final BiFunction<Integer, CharacteristicRatio, String> CALCULATE_CHARACTERISTIC_RATIO =
            (data, characteristicRatio) -> {
                if (data >= characteristicRatio.getSource()) {
                    return "来源";
                } else if (data <= characteristicRatio.getTransfer()) {
                    return "中转";
                } else if (data >= characteristicRatio.getPrecipitate()) {
                    return "沉淀";
                } else {
                    return "其他";
                }
            };

}
