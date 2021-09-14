/**
 * @作者 Mcj
 */
package com.zqykj.core;

import com.zqykj.client.util.ScrollState;
import org.elasticsearch.search.aggregations.Aggregations;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * <h1> stream query </h1>
 */
abstract class StreamQueries {

    static <T> SearchHitsIterator<T> streamResults(final int maxCount, final SearchScrollHits<T> searchHits,
                                                   final Function<String, SearchScrollHits<T>> continueScrollFunction,
                                                   final Consumer<List<String>> clearScrollConsumer) {
        Assert.notNull(searchHits, "searchHits must not be null.");
        Assert.notNull(searchHits.getScrollId(), "scrollId of searchHits must not be null.");
        Assert.notNull(continueScrollFunction, "continueScrollFunction must not be null.");
        Assert.notNull(clearScrollConsumer, "clearScrollConsumer must not be null.");
        final Aggregations aggregations = searchHits.getAggregations();
        final float maxScore = searchHits.getMaxScore();
        final long totalHits = searchHits.getTotalHits();
        final TotalHitsRelation totalHitsRelation = searchHits.getTotalHitsRelation();
        return new SearchHitsIterator<T>() {
            private volatile AtomicInteger currentCount = new AtomicInteger();
            private volatile Iterator<SearchHit<T>> currentScrollHits = searchHits.iterator();
            private volatile boolean continueScroll;
            private volatile ScrollState scrollState;

            {
                this.continueScroll = this.currentScrollHits.hasNext();
                this.scrollState = new ScrollState(searchHits.getScrollId());
            }

            public void close() {
                clearScrollConsumer.accept(this.scrollState.getScrollIds());
            }

            @Nullable
            public Aggregations getAggregations() {
                return aggregations;
            }

            public float getMaxScore() {
                return maxScore;
            }

            public long getTotalHits() {
                return totalHits;
            }

            public TotalHitsRelation getTotalHitsRelation() {
                return totalHitsRelation;
            }

            public boolean hasNext() {
                if (this.continueScroll && (maxCount <= 0 || this.currentCount.get() < maxCount)) {
                    if (!this.currentScrollHits.hasNext()) {
                        SearchScrollHits<T> nextPage = (SearchScrollHits) continueScrollFunction.apply(this.scrollState.getScrollId());
                        this.currentScrollHits = nextPage.iterator();
                        this.scrollState.updateScrollId(nextPage.getScrollId());
                        this.continueScroll = this.currentScrollHits.hasNext();
                    }

                    return this.currentScrollHits.hasNext();
                } else {
                    return false;
                }
            }

            public SearchHit<T> next() {
                if (this.hasNext()) {
                    this.currentCount.incrementAndGet();
                    return (SearchHit) this.currentScrollHits.next();
                } else {
                    throw new NoSuchElementException();
                }
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    private StreamQueries() {
    }
}
