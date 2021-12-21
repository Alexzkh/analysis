/**
 * @作者 Mcj
 */
package com.zqykj.repository.query;

import com.zqykj.domain.PageRequest;
import com.zqykj.domain.Pageable;
import com.zqykj.domain.Sort;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.support.IndicesOptions;
import org.springframework.lang.Nullable;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * <h1> 查询对象返回包装顶级接口 </h1>
 */
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

    int from();

    int size();

    <T extends Query> T addSort(Sort sort);

    /**
     * @return null if not set
     */
    @Nullable
    Sort getSort();

    /**
     * Add fields to be added as part of search request
     *
     * @param fields
     */
    void addFields(String... fields);

    void addExcludeFields(String... fields);

    List<String> getFields();

    List<String> getExcludeFields();

    /**
     * Add source filter to be added as part of search request
     */
    void addSourceFilter(SourceFilter sourceFilter);

    @Nullable
    SourceFilter getSourceFilter();

    /**
     * Get minimum score
     */
    float getMinScore();

    boolean getTrackScores();

    @Nullable
    String getPreference();

    void setPreference(String preference);

    void setHighlightQuery(HighlightQuery highlightQuery);

    default Optional<HighlightQuery> getHighlightQuery() {
        return Optional.empty();
    }

    void setTrackTotalHits(@Nullable Boolean trackTotalHits);

    @Nullable
    Collection<String> getIds();

    SearchType getSearchType();

    @Nullable
    IndicesOptions getIndicesOptions();

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

    @Nullable
    Duration getScrollTime();

    void setScrollTime(@Nullable Duration scrollTime);

    default boolean hasScrollTime() {
        return getScrollTime() != null;
    }
}
