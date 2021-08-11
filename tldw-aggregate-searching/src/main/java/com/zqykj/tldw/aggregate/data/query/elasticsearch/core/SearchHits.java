/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.data.query.elasticsearch.core;

import com.zqykj.infrastructure.util.Streamable;
import org.elasticsearch.search.aggregations.Aggregations;
import org.springframework.lang.Nullable;

import java.util.Iterator;
import java.util.List;

public interface SearchHits<T> extends Streamable<SearchHit<T>> {

    /**
     * @return the aggregations.
     */
    @Nullable
    Aggregations getAggregations();

    /**
     * @return the maximum score
     */
    float getMaxScore();

    /**
     * @param index position in List.
     * @return the {@link SearchHit} at position {index}
     * @throws IndexOutOfBoundsException on invalid index
     */
    SearchHit<T> getSearchHit(int index);

    /**
     * @return the contained {@link com.zqykj.tldw.aggregate.data.query.elasticsearch.core.SearchHit}s.
     */
    List<SearchHit<T>> getSearchHits();

    /**
     * @return the number of total hits.
     */
    long getTotalHits();

    @Nullable
    String getScrollId();

    /**
     * @return the relation for the total hits
     */
    TotalHitsRelation getTotalHitsRelation();

    /**
     * @return true if aggregations are available
     */
    default boolean hasAggregations() {
        return getAggregations() != null;
    }

    /**
     * @return whether the {@link SearchHits} has search hits.
     */
    default boolean hasSearchHits() {
        return !getSearchHits().isEmpty();
    }

    /**
     * @return an iterator for {@link SearchHit}
     */
    @Override
    default Iterator<SearchHit<T>> iterator() {
        return getSearchHits().iterator();
    }

    enum TotalHitsRelation {
        EQUAL_TO,
        GREATER_THAN_OR_EQUAL_TO,
        OFF
    }
}
