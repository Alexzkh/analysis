package com.zqykj.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @Description: TODO
 * @Author zhangkehou
 * @Date 2021/9/25
 */
public class NumberUtils {

    public static BigDecimal doubleTransform(Double d,int precision){

        BigDecimal bigDecimal = new BigDecimal(d);
       return bigDecimal.setScale(precision, RoundingMode.HALF_UP);
    }

    public static void main(String[] args) {
        BigDecimal bigDecimal = doubleTransform(8.339949463E7,2);
        System.out.println("***********"+bigDecimal);
        System.out.println("&&&&&&&&&"+doubleTransform(8.339949463E7,2));
    }
}
