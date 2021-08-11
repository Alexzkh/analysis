/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate;

import com.zqykj.infrastructure.util.CloseableIterator;
import com.zqykj.tldw.aggregate.data.query.elasticsearch.core.SearchHit;
import com.zqykj.tldw.aggregate.data.query.elasticsearch.core.SearchHits;
import org.elasticsearch.search.aggregations.Aggregations;
import org.springframework.lang.Nullable;

/**
 * @author Mcj
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
    SearchHits.TotalHitsRelation getTotalHitsRelation();

    /**
     * @return true if aggregations are available
     */
    default boolean hasAggregations() {
        return getAggregations() != null;
    }
}
