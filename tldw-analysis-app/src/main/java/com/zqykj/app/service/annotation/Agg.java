/**
 * @作者 Mcj
 */
package com.zqykj.app.service.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Agg {

    // 聚合名称
    String name() default "";
}
