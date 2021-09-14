package com.zqykj.annotations;


import java.lang.annotation.*;

/**
 * Elasticsearch Mapping
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface Mapping {

    String mappingPath() default "";
}
