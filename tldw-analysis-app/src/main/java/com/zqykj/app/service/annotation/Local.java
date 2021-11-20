package com.zqykj.app.service.annotation;

import java.lang.annotation.*;

/**
 * <h1> 资金战法本方标识 </h1>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Local {

    // 聚合名称
    String name() default "";

    // 实体特殊属性判断 eg. date
    String type() default "default";

    // 排序字段名称
    String sortName() default "";
}
