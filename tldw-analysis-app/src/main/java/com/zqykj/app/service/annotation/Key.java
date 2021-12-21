/**
 * @作者 Mcj
 */
package com.zqykj.app.service.annotation;

import java.lang.annotation.*;

/**
 * <h1> 聚合结果中的属性 </h1>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
@Documented
public @interface Key {

    // 后期优化,可以取多个值(改成String[] 数组类型)
    String name() default "hits";
}
