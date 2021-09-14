/**
 * @作者 Mcj
 */
package com.zqykj.mapping;

import org.springframework.lang.Nullable;

public class MappingException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MappingException(@Nullable String s) {
        super(s);
    }

    public MappingException(@Nullable String s, @Nullable Throwable throwable) {
        super(s, throwable);
    }
}
