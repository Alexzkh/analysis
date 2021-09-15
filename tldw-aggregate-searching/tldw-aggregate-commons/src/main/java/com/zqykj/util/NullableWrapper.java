/**
 * @作者 Mcj
 */
package com.zqykj.util;

import org.springframework.lang.Nullable;

public class NullableWrapper {

    @Nullable
    private final Object value;

    /**
     * Creates a new {@link NullableWrapper} for the given value.
     *
     * @param value can be {@literal null}.
     */
    public NullableWrapper(@Nullable Object value) {
        this.value = value;
    }

    /**
     * Returns the type of the contained value. Will fall back to {@link Object} in case the value is {@literal null}.
     *
     * @return will never be {@literal null}.
     */
    public Class<?> getValueType() {

        Object value = this.value;

        return value == null ? Object.class : value.getClass();
    }

    /**
     * Returns the backing value.
     *
     * @return the value can be {@literal null}.
     */
    @Nullable
    public Object getValue() {
        return value;
    }
}
