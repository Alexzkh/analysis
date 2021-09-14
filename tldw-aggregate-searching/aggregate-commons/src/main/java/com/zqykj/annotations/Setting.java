package com.zqykj.annotations;


import java.lang.annotation.*;

/**
 * Elasticsearch Setting(Mapping)
 */

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Setting {

    String settingPath() default "";

}
