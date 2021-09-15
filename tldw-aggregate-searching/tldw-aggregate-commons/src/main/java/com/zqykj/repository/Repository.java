package com.zqykj.repository;

import org.springframework.stereotype.Indexed;

/**
 * Central Repository marker interface. Captures the domain type to manage as well as the domain type's id type. General
 * purpose is to hold type information as well as being able to discover interfaces that extend this one during
 * classpath scanning for easy Spring bean creation.
 * <p>
 * Domain operations extending this interface can selectively expose CRUD methods by simply declaring methods of the
 * same signature as those declared in {@link CrudRepository}.
 *
 * @param <T>  the domain type the operations manages
 * @param <ID> the type of the id of the entity the operations manages
 * @Author zhangkehou
 * @Date 2021/8/17
 * @see CrudRepository
 */
@Indexed
public interface Repository<T, ID> {

}
