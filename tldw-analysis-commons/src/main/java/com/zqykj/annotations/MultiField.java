/**
 * @作者 Mcj
 */
package com.zqykj.annotations;

import java.lang.annotation.*;

/**
 * <h1> 用来处理Multi-field  </h1>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MultiField {

    /**
     * <h2> 主要字段 </h2>
     */
    Field mainField();

    /**
     * <h2> 内部字段 </h2>
     */
    InnerField[] otherFields() default {};
}
