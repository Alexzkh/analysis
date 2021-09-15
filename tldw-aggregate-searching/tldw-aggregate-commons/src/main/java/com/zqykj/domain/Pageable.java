/**
 * @作者 Mcj
 */
package com.zqykj.domain;

import java.util.Optional;

/**
 * <h1> 分页信息抽象接口 </h1>
 */
public interface Pageable {


    static Pageable unpaged() {
        return Unpaged.INSTANCE;
    }

    default boolean isUnpaged() {
        return !isPaged();
    }

    default boolean isPaged() {
        return true;
    }

    int getPageNumber();

    int getPageSize();

    long getOffset();

    Sort getSort();

    Pageable next();

    Pageable first();

    boolean hasPrevious();

    default Optional<Pageable> toOptional() {
        return isUnpaged() ? Optional.empty() : Optional.of(this);
    }
}
