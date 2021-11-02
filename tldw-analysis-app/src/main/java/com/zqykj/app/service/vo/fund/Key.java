/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund;

import java.lang.annotation.*;

/**
 * <h1> 聚合结果中的属性 </h1>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Key {
    String name() default "";
}
