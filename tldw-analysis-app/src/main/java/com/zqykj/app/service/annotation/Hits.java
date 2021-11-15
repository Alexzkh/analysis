/**
 * @作者 Mcj
 */
package com.zqykj.app.service.annotation;

import java.lang.annotation.*;

/**
 * <h1> 用于聚合中展示字段标记使用 </h1>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Hits {
}
