/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund;

import java.lang.annotation.*;

/**
 * <h1> 资金交易战法对方标识 </h1>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Opposite {

    String name() default "";

    String type() default "default";

    String sortName() default "";
}
