/**
 * @author Mcj
 */
package com.zqykj.parameters.annotation;

import java.lang.annotation.*;

/**
 * <h1> 聚合可选参数处理(不是每个聚合都有这样的参数) </h1>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface OptionalParam {
}
