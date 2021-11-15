/**
 * @作者 Mcj
 */
package com.zqykj.app.service.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Agg {

    String name() default "";
}
