/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.covert;

public interface EntityWriter<T, S> {


    void write(T source, S sink);
}
