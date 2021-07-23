package com.zqykj.tldw.aggregate.searching.esclientrhl.annotation;


import com.zqykj.tldw.aggregate.searching.esclientrhl.enums.Analyzer;
import com.zqykj.tldw.aggregate.searching.esclientrhl.enums.DataType;

import java.lang.annotation.*;

/**
 * The annotation is used to build index instructure mapping is added to elasticsearch entity field.
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface ESMapping {
    String field_name() default "";
    /**
     * Data type (include keyword type )
     */
    DataType datatype() default DataType.text_type;

    /**
     * Indirect keyword.
     */
    boolean keyword() default true;

    /**
     * Keywords ingore words.
     */
    int ignore_above() default 256;

    /**
     * Whether to support ngrem, efficient full text search prompt.
     */
    boolean ngram() default false;

    /**
     * Whether to support suggest,efficient prefix search prompt.
     */
    boolean suggest() default false;

    /**
     * Index analyzeer settings.
     */
    Analyzer analyzer() default Analyzer.standard;

    /**
     * Search content word analyzer settings.
     */
    Analyzer search_analyzer() default Analyzer.standard;

    /**
     * Whether allow search .
     */
    boolean allow_search() default true;

    /**
     * Replace _all .
     */
    String copy_to() default "";

    /**
     * null_value specifies that the default empty string does not add null to mapping_value.
     * <p>
     * If the value is null, it is processed according to the 'null' specified by the annotation_ Value 'can be found by querying.
     * <p>
     * It should be noted that it should be distinguished from no field at all (no field needs to be queried with exists query)
     * <p>
     * Null is recommended_ VALUE
     */
    String null_value() default "";

    /**
     * The type corresponding to nested is object.class by default.
     * <p>
     * For datatype, it is nested_ Only the type of type needs to be annotated.
     * <p>
     * Through this annotation, the index of nested type is generated.
     */
    Class nested_class() default Object.class;
}
