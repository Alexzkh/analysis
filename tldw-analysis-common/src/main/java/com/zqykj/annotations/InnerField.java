/**
 * @作者 Mcj
 */
package com.zqykj.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <h1> Field 中的内部 Field </h1>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface InnerField {

    String suffix();

    FieldType type();

    boolean index() default true;

    DateFormat format() default DateFormat.none;

    String pattern() default "";

    boolean store() default false;

    boolean fielddata() default false;

    String searchAnalyzer() default "";

    String analyzer() default "";

    String normalizer() default "";

    /**
     * @since 4.0
     */
    int ignoreAbove() default -1;

    /**
     * @since 4.0
     */
    boolean coerce() default true;

    /**
     * @since 4.0
     */
    boolean docValues() default true;

    /**
     * @since 4.0
     */
    boolean ignoreMalformed() default false;

    /**
     * @since 4.0
     */
    IndexOptions indexOptions() default IndexOptions.none;

    /**
     * @since 4.0
     */
    boolean indexPhrases() default false;

    /**
     * implemented as array to enable the empty default value
     *
     * @since 4.0
     */
    IndexPrefixes[] indexPrefixes() default {};

    /**
     * @since 4.0
     */
    boolean norms() default true;

    /**
     * @since 4.0
     */
    String nullValue() default "";

    /**
     * @since 4.0
     */
    int positionIncrementGap() default -1;

    /**
     * @since 4.0
     */
    Similarity similarity() default Similarity.Default;

    /**
     * @since 4.0
     */
    TermVector termVector() default TermVector.none;

    /**
     * @since 4.0
     */
    double scalingFactor() default 1;

    /**
     * @since 4.0
     */
    int maxShingleSize() default -1;

    /**
     * to be used in combination with {@link FieldType#Rank_Feature}
     *
     * @since 4.1
     */
    boolean positiveScoreImpact() default true;

    /**
     * @since 4.1
     */
    boolean eagerGlobalOrdinals() default false;

    /**
     * @since 4.1
     */
    NullValueType nullValueType() default NullValueType.String;
}
