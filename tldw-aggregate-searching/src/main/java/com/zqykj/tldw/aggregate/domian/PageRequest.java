/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.domian;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * <h1> 基础分页逻辑实现 </h1>
 */
public class PageRequest extends AbstractPageRequest {


    private static final long serialVersionUID = -7586302327937393059L;

    private Sort sort;

    public PageRequest(int page, int size, Sort sort) {
        super(page, size);

        Assert.notNull(sort, "Sort must not be null!");

        this.sort = sort;
    }

    public static PageRequest of(int page, int size) {

        return of(page, size, Sort.unsorted());
    }

    public static PageRequest of(int page, int size, Sort sort) {
        return new PageRequest(page, size, sort);
    }

    public static PageRequest of(int page, int size, Sort.Direction direction, String... properties) {
        return of(page, size, Sort.by(direction, properties));
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    @Override
    public boolean equals(@Nullable Object obj) {

        if (this == obj) {
            return true;
        }

        if (!(obj instanceof PageRequest)) {
            return false;
        }

        PageRequest that = (PageRequest) obj;

        return super.equals(that) && this.sort.equals(that.sort);
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + sort.hashCode();
    }

    @Override
    public String toString() {
        return String.format("Page request [number: %d, size %d, sort: %s]", getPageNumber(), getPageSize(), sort);
    }
}
