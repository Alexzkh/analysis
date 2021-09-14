/**
 * @作者 Mcj
 */
package com.zqykj.core;

import com.zqykj.util.CloseableIterator;
import org.elasticsearch.search.aggregations.Aggregations;
import org.springframework.lang.Nullable;

/**
 * <h1>  A {@link SearchHitsIterator} encapsulates {@link SearchHit} results that can be wrapped in a Java 8
 * {@link java.util.stream.Stream} </h1>
 */
public interface SearchHitsIterator<T> extends CloseableIterator<SearchHit<T>> {

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
     * @return the number of total hits.
     */
    long getTotalHits();

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
}
