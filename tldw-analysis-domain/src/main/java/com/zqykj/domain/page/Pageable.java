/**
 * @作者 Mcj
 */
package com.zqykj.domain.page;

import java.util.Optional;

/**
 * <h1> 分页信息抽象接口 </h1>
 */
public interface Pageable {


    static Pageable unpaged() {
        return Unpaged.INSTANCE;
    }

    default boolean isPaged() {
        return true;
    }

    int getPageNumber();

    int getPageSize();

    long getOffset();

    Sort getSort();


    enum Unpaged implements Pageable {

        INSTANCE;

        @Override
        public boolean isPaged() {
            return false;
        }

        @Override
        public int getPageNumber() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getPageSize() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getOffset() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Sort getSort() {
            throw new UnsupportedOperationException();
        }
    }

    default Optional<Pageable> toOptional() {
        return isUnpaged() ? Optional.empty() : Optional.of(this);
    }

    default boolean isUnpaged() {
        return !isPaged();
    }
}
