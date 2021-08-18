package com.zqykj.tldw.aggregate;

import org.springframework.stereotype.Indexed;

/**
 * Central operations marker interface. Captures the domain type to manage as well as the domain type's id type. General
 * purpose is to hold type information as well as being able to discover interfaces that extend this one during
 * classpath scanning for easy Spring bean creation.
 * <p>
 * Domain operations extending this interface can selectively expose CRUD methods by simply declaring methods of the
 *  same signature as those declared in {@link CRUDOperations}.
 *
 * @see CRUDOperations
 * @param <T> the domain type the operations manages
 * @param <M> the type of the id of the entity the operations manages
 * @Author zhangkehou
 * @Date 2021/8/17
 */
@Indexed
public interface BaseOperations<T, M> {

}





