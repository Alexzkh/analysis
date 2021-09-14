package com.zqykj.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface HighlightField {

    /**
     * The name of the field to apply highlighting to. This must be the field name of the entity's property, not the name
     * of the field in the index mappings.
     */
    String name() default "";

    HighlightParameters parameters() default @HighlightParameters;
}
