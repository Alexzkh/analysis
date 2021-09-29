/**
 * @author Mcj
 */
package com.zqykj.parameters.annotation;

import java.lang.annotation.*;

/**
 * <h1> 聚合默认参数处理 </h1>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface DefaultParameter {
}
