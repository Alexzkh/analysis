package com.zqykj.app.service.vo.fund;

import java.lang.annotation.*;

/**
 * <h1> 资金战法本方标识 </h1>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Local {

    String name() default "";

    String type() default "default";

    String sortName() default "";
}
