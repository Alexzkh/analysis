package com.zqykj.annotations;

import java.lang.annotation.*;

/**
 * <h1> Es的高亮查询能使用得到 </h1>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Highlight {

    HighlightParameters parameters() default @HighlightParameters;

    HighlightField[] fields();
}
