/**
 * @作者 Mcj
 */
package com.zqykj.core;

import org.springframework.lang.Nullable;

public interface SearchScrollHits<T> extends SearchHits<T> {

    /**
     * @return the scroll id
     */
    @Nullable
    String getScrollId();
}
