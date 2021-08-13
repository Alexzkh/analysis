/**
 * @作者 Mcj
 */
package com.zqykj.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface NoRepositoryBean {

}
