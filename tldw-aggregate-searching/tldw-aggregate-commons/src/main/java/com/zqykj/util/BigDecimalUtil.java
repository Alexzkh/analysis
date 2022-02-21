/**
 * @作者 Mcj
 */
package com.zqykj.util;

import java.math.BigDecimal;

public class BigDecimalUtil {

    private BigDecimalUtil() {

    }

    public static long longValue(String date) {

        return new BigDecimal(date).longValue();
    }

    public static BigDecimal value(double value) {

        BigDecimal bigDecimal = new BigDecimal(value);
        return bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public static BigDecimal value(String value) {

        BigDecimal bigDecimal = new BigDecimal(value);
        return bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public static BigDecimal value(BigDecimal value) {

        return value.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public static BigDecimal add(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.add(b2).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public static BigDecimal add(BigDecimal v1, BigDecimal v2) {
        BigDecimal add = v1.add(v2);
        return add.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public static BigDecimal add(String v1, String v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        BigDecimal add = b1.add(b2);
        return add.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public static BigDecimal sub(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.subtract(b2).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public static BigDecimal sub(String v1, String v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.subtract(b2).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public static BigDecimal mul(double v1, double v2, int scale, int roundingMode) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.multiply(b2).setScale(scale, roundingMode);
    }

    public static BigDecimal mul(double v1, double v2) {
        return mul(v1, v2, 2, BigDecimal.ROUND_HALF_UP);
    }

    public static BigDecimal mul(BigDecimal v1, int v2) {
        BigDecimal b2 = new BigDecimal(v2);
        return v1.multiply(b2).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public static BigDecimal mul(BigDecimal v1, int v2, int scale) {
        BigDecimal b2 = new BigDecimal(v2);
        return v1.multiply(b2).setScale(scale, BigDecimal.ROUND_HALF_UP);
    }

    public static BigDecimal mul(String v1, String v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.multiply(b2);
    }

    public static BigDecimal div(double v1, double v2, int scale, int roundingMode) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.divide(b2, scale, roundingMode);
    }

    public static BigDecimal div(double v1, double v2, int scale) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP);
    }

    public static BigDecimal div(int v1, int v2, int scale) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP);
    }

    public static BigDecimal div(double v1, double v2) {
        return div(v1, v2, 2, BigDecimal.ROUND_HALF_UP);
    }

    public static BigDecimal divReserveFour(double v1, double v2) {
        return div(v1, v2, 4, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * <h2> 保留小数的后四位 </h2>
     */
    public static BigDecimal divReserveFour(BigDecimal v1, BigDecimal v2) {
        return div(v1, v2, 4);
    }

    public static BigDecimal div(BigDecimal v1, BigDecimal v2, int scale) {
        return v1.divide(v2, scale, BigDecimal.ROUND_HALF_UP);//四舍五入,保留指定位小数
    }

    /**
     * <h2> 保留小数的后四位 </h2>
     */
    public static BigDecimal divReserveFour(int v1, int v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.divide(b2, 4, BigDecimal.ROUND_HALF_UP);//四舍五入,保留2位小数
    }

    public static BigDecimal div(String v1, String v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.divide(b2, 2, BigDecimal.ROUND_HALF_UP);
    }

}
