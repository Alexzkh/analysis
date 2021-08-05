package com.zqykj.tldw.aggregate.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Query {

    // query to be used when executing query. May contain placeholders eg. ?0
    String value() default "";
}
