/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.domian;

import java.util.function.Function;

/**
 * <h1> 分页信息 数据包装 </h1>
 */
public interface Page<T> extends Iterable<T> {

    /**
     * <h2> 总页数 </h2>
     */
    int getTotalPages();

    /**
     * <h2> 总数据量 </h2>
     */
    long getTotalElements();


    <U> Page<U> map(Function<? super T, ? extends U> converter);
}
