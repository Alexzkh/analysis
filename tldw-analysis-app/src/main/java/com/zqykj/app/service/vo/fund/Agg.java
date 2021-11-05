/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Agg {

    String name() default "";
}
