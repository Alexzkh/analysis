/**
 * @作者 Mcj
 */
package com.zqykj.coverter;

/**
 * <h1> 从特定来源读取对象的接口 </h1>
 */
public interface EntityReader<T, S> {

    /**
     * <h2> 将给定的数据源 读入指定的Class 类型 </h2>
     *
     * @param type   they type to convert the given source to.
     * @param source the source to create an object of the given type from.
     */
    <R extends T> R read(Class<R> type, S source);
}
