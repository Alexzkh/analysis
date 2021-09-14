package com.zqykj.annotations;

/**
 *
 */
public @interface IndexPrefixes {
    int MIN_DEFAULT = 2;
    int MAX_DEFAULT = 2;

    int minChars() default MIN_DEFAULT;

    int maxChars() default MAX_DEFAULT;
}
