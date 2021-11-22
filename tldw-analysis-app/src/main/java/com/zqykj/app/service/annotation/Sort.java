/**
 * @作者 Mcj
 */
package com.zqykj.app.service.annotation;

import java.lang.annotation.*;

/**
 * <h1> 聚合排序注解 </h1>
 * <p>
 * 只对数值类型排序
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
@Documented
public @interface Sort {

    // 排序名称
    String name() default "";

    // DESC / ASC
    String direction() default "DESC";
}
