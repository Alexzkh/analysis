package com.zqykj.app.service.transform;

import com.zqykj.domain.Range;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 数值区间转换
 * @Author zhangkehou
 * @Date 2021/10/11
 */
public class NumericalConversion {


    /**
     * 计算各区间的x轴的值是将当前最大值除以区间个数来过去到的,结果向上取正得值.
     *
     * @param max:    区间最大值
     * @param number: 区间个数
     * @return: java.util.List<com.zqykj.domain.Range>
     **/
    public static List<Range> intervalConversion(Double max, Integer number) {
        int interval = (int) (max / number);
        List<Range> ranges = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            Range range = new Range((double) i * interval, (double) (i + 1) * interval);
            ranges.add(range);
        }
        return ranges;
    }

}
