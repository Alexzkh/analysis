/**
 * @作者 Mcj
 */
package com.zqykj.app.service.annotation;

import java.lang.annotation.*;

/**
 * @Description: TODO
 * @Author zhangkehou
 * @Date 2021/11/22
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
@Documented
public @interface Sort {

    String name() default "";

    String direction() default "desc";
}
