/**
 * @作者 Mcj
 */
package com.zqykj.repository.query;


import com.zqykj.domain.EntityClass;
import com.zqykj.domain.Pageable;
import com.zqykj.domain.Routing;
import com.zqykj.domain.Sort;

import java.util.Iterator;

/**
 * <h1> 参数访问器 </h1>
 */
public interface ParameterAccessor extends Iterable<Object> {

    /**
     * <h2> Returns the {@link Pageable} of the parameters, if available. Returns {@code null} otherwise. </h2>
     */
    Pageable getPageable();

    /**
     * <h2> Returns the sort instance to be used for query creation. Will use a {@link Sort} parameter if available or the
     * {@link Sort} contained in a {@link Pageable} if available. Returns {@code null} if no {@link Sort} can be found.  </h2>
     */
    Sort getSort();

    EntityClass getDomain();

    Routing getRouting();

    /**
     * <h2> 返回给定索引的绑定值(用于方法参数获取指定位置索引的参数值) </h2>
     *
     * @param index index
     */
    Object getBindableValue(int index);

    /**
     * <h2> Returns whether one of the bindable parameter values is {@literal null}. </h2>
     */
    boolean hasBindableNullValue();

    /**
     * <h2> 返回所有参数的迭代器 </h2>
     */
    Iterator<Object> iterator();
}
