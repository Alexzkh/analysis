package com.zqykj.common.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: Operator parameter
 * @Author zhangkehou
 * @Date 2021/9/30
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OperatorParam {


    /**
     * operator
     */
    private Operator operator;

    /**
     * operator value.
     */
    private Double operatorValue;

    /**
     * Representative section opening and closing .if value equals true , the section is closing ,otherwise is opening .
     */
    private boolean include;
}
