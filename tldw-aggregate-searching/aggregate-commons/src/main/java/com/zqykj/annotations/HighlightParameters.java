package com.zqykj.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface HighlightParameters {
    String boundaryChars() default "";

    int boundaryMaxScan() default -1;

    String boundaryScanner() default "";

    String boundaryScannerLocale() default "";

    /**
     * only used for {@link Highlight}s.
     */
    String encoder() default "";

    boolean forceSource() default false;

    String fragmenter() default "";

    /**
     * only used for {@link HighlightField}s.
     */
    int fragmentOffset() default -1;

    int fragmentSize() default -1;

    /**
     * only used for {@link HighlightField}s.
     */
    String[] matchedFields() default {};

    int noMatchSize() default -1;

    int numberOfFragments() default -1;

    String order() default "";

    int phraseLimit() default -1;

    String[] preTags() default {};

    String[] postTags() default {};

    boolean requireFieldMatch() default true;

    /**
     * only used for {@link Highlight}s.
     */
    String tagsSchema() default "";

    String type() default "";
}
