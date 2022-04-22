/**
 * @作者 Mcj
 */
package com.zqykj.app.service.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
@Documented
public @interface Agg {

    // 聚合名称
    String name() default "";

    // 聚合中展示字段标记
    boolean showField() default false;
}
