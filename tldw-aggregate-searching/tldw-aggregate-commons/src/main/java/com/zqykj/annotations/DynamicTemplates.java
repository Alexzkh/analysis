/**
 * @作者 Mcj
 */
package com.zqykj.annotations;

import java.lang.annotation.*;

/**
 * <h1> elasticsearch 动态模板映射配置  </h1>
 */
@Persistent
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface DynamicTemplates {

    String mappingPath() default "";
}
