package com.zqykj.common.enums;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 交易统计-柱状图实体类
 * @Author zhangkehou
 * @Date 2021/10/11
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HistogramStatistic {

    /**
     * 柱状图横坐标
     */
    private String abscissa;

    /**
     * 柱状图纵坐标
     */
    private long ordinate;

}