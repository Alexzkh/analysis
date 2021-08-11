/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.data.query.elasticsearch;

import com.zqykj.domain.page.PageRequest;
import com.zqykj.domain.page.Pageable;
import com.zqykj.domain.page.Sort;
import org.elasticsearch.action.search.SearchType;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Optional;

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

    List<String> getFields();

    SearchType getSearchType();

    default Optional<HighlightQuery> getHighlightQuery() {
        return Optional.empty();
    }

    @Nullable
    Boolean getTrackTotalHits();

    void setTrackTotalHitsUpTo(@Nullable Integer trackTotalHitsUpTo);

    @Nullable
    Integer getTrackTotalHitsUpTo();

    @Nullable
    String getRoute();

    default boolean isLimiting() {
        return false;
    }

    @Nullable
    default Integer getMaxResults() {
        return null;
    }
}
