/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.covert;

public interface EntityReader<T, S> {

    /**
     * <h2> 将给定的数据源 读入指定的Class 类型 </h2>
     *
     * @param type   they type to convert the given source to.
     * @param source the source to create an object of the given type from.
     */
    <R extends T> R read(Class<R> type, S source);
}
