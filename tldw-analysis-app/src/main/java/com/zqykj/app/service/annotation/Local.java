package com.zqykj.app.service.annotation;

import java.lang.annotation.*;

/**
 * <h1> 资金战法一方标识 </h1>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
@Documented
public @interface Local {

    // 聚合名称
    String name() default "";

    // 排序字段名称
    String sortName() default "";

    // 聚合中展示字段标记
    boolean showField() default false;
}
