/**
 * @作者 Mcj
 */
package com.zqykj.core;

import com.zqykj.domain.Page;

/**
 * <h1> 滚动查询分页 </h1>
 */
public interface ScrolledPage<T> extends Page<T> {

    String getScrollId();
}
