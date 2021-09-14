/**
 * @作者 Mcj
 */
package com.zqykj.annotations;

import org.springframework.stereotype.Indexed;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <h1> 标识持久类型、字段和参数的注释 </h1>
 */
@Indexed
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
public @interface Persistent {
}
