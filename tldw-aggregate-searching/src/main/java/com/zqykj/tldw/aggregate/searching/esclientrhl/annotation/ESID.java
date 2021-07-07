package com.zqykj.tldw.aggregate.searching.esclientrhl.annotation;

import java.lang.annotation.*;

/**
 * The annotation of elasticsearch entity to be used identify ID is added to elasticsearch entity field .
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface ESID {
}
