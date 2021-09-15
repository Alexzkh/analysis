/**
 * @作者 Mcj
 */
package com.zqykj.core;

import com.zqykj.domain.Page;

public interface SearchPage<T> extends Page<SearchHit<T>> {

    SearchHits<T> getSearchHits();
}
