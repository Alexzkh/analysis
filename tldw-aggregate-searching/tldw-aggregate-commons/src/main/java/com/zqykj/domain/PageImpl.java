/**
 * @作者 Mcj
 */
package com.zqykj.domain;

import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Mcj
 */
public class PageImpl<T> implements Serializable, Page<T> {

    private static final long serialVersionUID = -8643594973304287900L;
    private final List<T> content = new ArrayList<>();
    private final Pageable pageable;
    public PageImpl(List<T> content, Pageable pageable, long total) {

        Assert.notNull(content, "Content must not be null!");
        Assert.notNull(pageable, "Pageable must not be null!");

        this.content.addAll(content);
        this.pageable = pageable;
        this.total = pageable.toOptional().filter(it -> !content.isEmpty())
                .filter(it -> it.getOffset() + it.getPageSize() > total)
                .map(it -> it.getOffset() + content.size())
                .orElse(total);
    }

    private final long total;

    public PageImpl(List<T> content) {
        this(content, Pageable.unpaged(), null == content ? 0 : content.size());
    }

    @Override
    public int getTotalPages() {
        return getSize() == 0 ? 1 : (int) Math.ceil((double) total / (double) getSize());
    }

    @Override
    public long getTotalElements() {
        return total;
    }

    @Override
    public <U> Page<U> map(Function<? super T, ? extends U> converter) {

        return new PageImpl<>(getConvertedContent(converter), getPageable(), total);
    }


    @Override
    public Iterator<T> iterator() {
        return content.iterator();
    }

    protected <U> List<U> getConvertedContent(Function<? super T, ? extends U> converter) {

        Assert.notNull(converter, "Function must not be null!");

        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(), 0), false)
                .map(converter).collect(Collectors.toList());
    }

    /**
     * <h2> 获取分页每页显示条数 </h2>
     */
    @Override
    public int getSize() {
        return pageable.isPaged() ? pageable.getPageSize() : content.size();
    }

    @Override
    public int getNumber() {
        return pageable.isPaged() ? pageable.getPageNumber() : 0;
    }

    /**
     * <h2> 获取分页数据 </h2>
     */
    @Override
    public List<T> getContent() {
        return Collections.unmodifiableList(content);
    }

    @Override
    public Sort getSort() {
        return pageable.getSort();
    }

    @Override
    public Pageable getPageable() {
        return pageable;
    }

    public boolean hasContent() {
        return !content.isEmpty();
    }

    public boolean hasPrevious() {
        return getNumber() > 0;
    }

    @Override
    public Pageable nextPageable() {
        return hasNext() ? pageable.next() : Pageable.unpaged();
    }

    @Override
    public Pageable previousPageable() {
        return null;
    }

    public boolean isFirst() {
        return !hasPrevious();
    }

    @Override
    public boolean isLast() {
        return false;
    }

    @Override
    public boolean hasNext() {
        return false;
    }

}
