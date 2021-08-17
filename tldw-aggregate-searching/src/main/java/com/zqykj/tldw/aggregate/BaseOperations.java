package com.zqykj.tldw.aggregate;

import org.springframework.stereotype.Indexed;

/**
 * @param <T> the domain type .
 * @param <M> the type of id of the domain entity .
 * @Description: The basic data operations.
 * <p>
 * The implementions or successors can be mongodb 、solr 、elasticsearch even hbase.
 * you can also do something special in your operations what the implementions of this.
 * <p>
 * @Author zhangkehou
 * @Date 2021/8/5
 */
@Indexed
public interface BaseOperations<T, M> {

}





