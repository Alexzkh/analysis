package com.zqykj.annotations;


import java.lang.annotation.*;

/**
 * Annotation to set the dynamic mapping mode
 * {@see <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/dynamic.html">elasticsearch doc</a>}
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
@Documented
public @interface DynamicMapping {

    DynamicMappingValue value() default DynamicMappingValue.True;
}

enum DynamicMappingValue {
    True, False, Strict
}
