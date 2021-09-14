package com.zqykj.annotations;


import java.lang.annotation.*;

/**
 * <h1> 设置动态映射模式的注解 </h1>
 * {@see <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/dynamic.html">
 * elasticsearch doc</a>}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
@Documented
public @interface DynamicMapping {

    DynamicMappingValue value() default DynamicMappingValue.True;
}