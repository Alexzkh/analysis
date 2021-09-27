package com.zqykj.infrastructure.util;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

/**
 * @Description: TODO
 * @Author zhangkehou
 * @Date 2021/9/25
 */
public class NumberUtils {

    /**
     * 保留2位小数
     *
     * @param v
     * @return
     */
    public static final double parseDouble(String v) {
        return parseDouble(v, 2);
    }

    /**
     * 保留2位小数
     *
     * @param v
     * @return
     */
    public static final double parseDouble(String v, int scale) {
        if (StringUtils.isEmpty(v)) {
            return 0;
        }
        try {
            double d = Double.parseDouble(v);
            BigDecimal b = new BigDecimal(d);
            d = b.setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
            return d;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static String doubleFormat(double d) {
        return String.format("%.2f", d);
    }


}
