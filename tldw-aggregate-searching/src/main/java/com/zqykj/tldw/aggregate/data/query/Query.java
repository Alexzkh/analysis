/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.data.query;

import com.zqykj.tldw.aggregate.domian.PageRequest;
import com.zqykj.tldw.aggregate.domian.Pageable;
import com.zqykj.tldw.aggregate.domian.Sort;
import org.springframework.lang.Nullable;

public interface Query {

    int DEFAULT_PAGE_SIZE = 10;
    Pageable DEFAULT_PAGE = PageRequest.of(0, DEFAULT_PAGE_SIZE);

    <T extends Query> T setPageable(Pageable pageable);

    /**
     * Get page settings if defined
     *
     * @return
     */
    Pageable getPageable();

    <T extends Query> T addSort(Sort sort);

    /**
     * @return null if not set
     */
    @Nullable
    Sort getSort();
}
