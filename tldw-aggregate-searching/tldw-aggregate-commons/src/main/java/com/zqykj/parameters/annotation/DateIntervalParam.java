/**
 * @author Mcj
 */
package com.zqykj.parameters.annotation;

import java.lang.annotation.*;

/**
 * <h1> 聚合日期间隔参数处理 </h1>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface DateIntervalParam {
}
