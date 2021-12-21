package com.zqykj.infrastructure.compare;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.beans.Transient;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description: 内存排序基础bean
 * @Author zhangkehou
 * @Date 2021/12/15
 */
public interface BaseCompareBean {

    Logger logger = LoggerFactory.getLogger(BaseCompareBean.class);

    @Transient
    default Comparable tryBestToFindCompareValue(String fieldName) {
        Comparable value = tryBestToFindFieldValue(fieldName, Comparable.class);
        if (value instanceof String) {
            //有一些数值在响应报文里用的String，为了避免String.compare的影响，尝试转BigDecimal进行比较，若value不是数值，则依旧用原值比较
            try {
                return new BigDecimal(value.toString().trim().replaceAll(",", "").replaceAll("%", ""));
            } catch (Exception e) {
                logger.error("排序时String类型转换为BigDecimal类型失败 原始值 {}", value);
            }
        }
        return value;
    }

    /**
     * @param fieldName 支持snake风格，可自动转换为驼峰风格再取值；若失败，会尝试用蛇形风格直接取值
     * @return
     */
    @Transient
    default <T> T tryBestToFindFieldValue(String fieldName, Class<T> c) {
        //因为传入的排序字段为snake风格
        String humpName = lineToHump(fieldName);
        try {
            return simpleFindFieldValue(humpName, c);
        } catch (Exception e) {
            try {
                return simpleFindFieldValue(fieldName, c);
            } catch (Exception e2) {
                logger.warn("找不到要目标排序字段 {}", fieldName);
                return null;
            }
        }
    }

    /**
     * @param str: snake式命名的字段。eg:f_parent_no_leader -> f_parent_no_leader
     * @return: java.lang.String
     **/
    default String lineToHump(String str) {
        str = str.toLowerCase();
        Pattern linePattern = Pattern.compile("_(\\w)");
        Matcher matcher = linePattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 简单获取指定属性名的属性值
     *
     * @param fieldName
     * @return
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    @Transient
    default <T> T simpleFindFieldValue(String fieldName, Class<T> c) throws IllegalAccessException {
        Field field = ReflectionUtils.findField(getClass(), fieldName);
        field.setAccessible(true);
        return c.cast(field.get(this));
    }

    default boolean abnormal() {
        return false;
    }


}
