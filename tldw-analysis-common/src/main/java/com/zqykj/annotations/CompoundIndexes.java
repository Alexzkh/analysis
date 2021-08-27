package com.zqykj.annotations;

import java.lang.annotation.*;

/**
 * Container annotation that allows to collect multiple {@link CompoundIndex} annotations.
 * <p>
 * Can be used natively, declaring several nested {@link CompoundIndex} annotations. Can also be used in conjunction
 * with Java 8's support for <em>repeatable annotations</em>, where {@link CompoundIndex} can simply be declared several
 * times on the same {@linkplain ElementType#TYPE type}, implicitly generating this container annotation.
 */
@Target({ ElementType.TYPE })
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface CompoundIndexes {

	CompoundIndex[] value();

}
