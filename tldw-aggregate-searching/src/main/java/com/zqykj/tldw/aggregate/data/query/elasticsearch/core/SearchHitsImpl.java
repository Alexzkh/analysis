/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.data.query.elasticsearch.core;

import com.zqykj.infrastructure.util.Lazy;
import org.elasticsearch.search.aggregations.Aggregations;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;

/**
 *
 */
public class SearchHitsImpl<T> implements SearchHits<T> {
    private final long totalHits;
    private final TotalHitsRelation totalHitsRelation;
    private final float maxScore;
    @Nullable
    private final String scrollId;
    private final List<? extends SearchHit<T>> searchHits;
    private final Lazy<List<SearchHit<T>>> unmodifiableSearchHits;
    @Nullable
    private final Aggregations aggregations;

    /**
     * @param totalHits         the number of total hits for the search
     * @param totalHitsRelation the relation {@see TotalHitsRelation}, must not be {@literal null}
     * @param maxScore          the maximum score
     * @param scrollId          the scroll id if available
     * @param searchHits        must not be {@literal null}
     * @param aggregations      the aggregations if available
     */
    public SearchHitsImpl(long totalHits, TotalHitsRelation totalHitsRelation, float maxScore, @Nullable String scrollId,
                          List<? extends SearchHit<T>> searchHits, @Nullable Aggregations aggregations) {

        Assert.notNull(searchHits, "searchHits must not be null");

        this.totalHits = totalHits;
        this.totalHitsRelation = totalHitsRelation;
        this.maxScore = maxScore;
        this.scrollId = scrollId;
        this.searchHits = searchHits;
        this.aggregations = aggregations;
        this.unmodifiableSearchHits = Lazy.of(() -> Collections.unmodifiableList(searchHits));
    }

    // region getter
    @Override
    public long getTotalHits() {
        return totalHits;
    }

    @Override
    public TotalHitsRelation getTotalHitsRelation() {
        return totalHitsRelation;
    }

    @Override
    public float getMaxScore() {
        return maxScore;
    }

    @Override
    @Nullable
    public String getScrollId() {
        return scrollId;
    }

    @Override
    public List<SearchHit<T>> getSearchHits() {
        return unmodifiableSearchHits.get();
    }
    // endregion

    // region SearchHit access
    @Override
    public SearchHit<T> getSearchHit(int index) {
        return searchHits.get(index);
    }
    // endregion

    @Override
    public String toString() {
        return "SearchHits{" +
                "totalHits=" + totalHits +
                ", totalHitsRelation=" + totalHitsRelation +
                ", maxScore=" + maxScore +
                ", scrollId='" + scrollId + '\'' +
                ", searchHits={" + searchHits.size() + " elements}" +
                ", aggregations=" + aggregations +
                '}';
    }

    // region aggregations
    @Override
    @Nullable
    public Aggregations getAggregations() {
        return aggregations;
    }
    // endregion
}
