/**
 * @作者 Mcj
 */
package com.zqykj.support;

import org.elasticsearch.search.SearchHits;

/**
 * <h1> elasticsearch 搜索命中数据工具类 </h1>
 */
public final class SearchHitsUtil {

    private SearchHitsUtil() {
    }

    public static long getTotalCount(SearchHits searchHits) {

        // 搜索命中的总数据量
        return searchHits.getTotalHits().value;
    }
}
