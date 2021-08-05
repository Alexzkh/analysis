/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.domian;

import java.io.Serializable;

/**
 * Abstract Java Bean implementation of {@link Pageable}.
 */
public abstract class AbstractPageRequest implements Pageable, Serializable {

    private static final long serialVersionUID = 4593306190271996406L;

    private final int page;

    private final int size;

    public AbstractPageRequest(int page, int size) {

        if (page < 0) {
            throw new IllegalArgumentException("Page index must not be less then zero!");
        }
        if (size < 1) {
            throw new IllegalArgumentException("Page size must not be less then one!");
        }
        this.page = page;
        this.size = size;
    }

    public int getPageSize() {
        return size;
    }

    public int getPageNumber() {
        return page;
    }

    public long getOffset() {
        return (long) page * (long) size;
    }

    public boolean hasPrevious() {
        return page > 0;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;

        result = prime * result + page;
        result = prime * result + size;

        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        AbstractPageRequest other = (AbstractPageRequest) obj;
        return this.page == other.page && this.size == other.size;
    }
}
