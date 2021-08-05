/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.annotations;

import java.lang.annotation.*;

/**
 * <h1> 用于扫描打上了@Document 直接的entity </h1>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface ScanEntity {

    String basePackages() default "";
}
