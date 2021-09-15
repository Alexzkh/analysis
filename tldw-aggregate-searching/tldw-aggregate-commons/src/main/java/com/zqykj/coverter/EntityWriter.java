
/**
 * @作者 Mcj
 */
package com.zqykj.coverter;

/**
 * <h1> 将对象写入存储特定接收器的接口 </h1>
 */
public interface EntityWriter<T, S> {


    void write(T source, S sink);
}
