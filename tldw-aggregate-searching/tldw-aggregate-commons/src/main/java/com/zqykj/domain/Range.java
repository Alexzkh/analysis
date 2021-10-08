package com.zqykj.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: range聚合入参
 * @Author zhangkehou
 * @Date 2021/9/28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Range {
    private Double from;
    private Double to;

}
