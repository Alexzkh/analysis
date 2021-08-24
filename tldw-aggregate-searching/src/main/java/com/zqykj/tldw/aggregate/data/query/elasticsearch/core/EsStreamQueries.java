/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.data.query.elasticsearch.core;

import com.zqykj.infrastructure.util.ScrollState;
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
 *
 */
public abstract class EsStreamQueries {

    public static <T> SearchHitsIterator<T> streamResults(int maxCount, SearchHits<T> searchHits,
                                                          Function<String, SearchHits<T>> continueScrollFunction, Consumer<List<String>> clearScrollConsumer) {

        Assert.notNull(searchHits, "searchHits must not be null.");
        Assert.notNull(searchHits.getScrollId(), "scrollId of searchHits must not be null.");
        Assert.notNull(continueScrollFunction, "continueScrollFunction must not be null.");
        Assert.notNull(clearScrollConsumer, "clearScrollConsumer must not be null.");

        Aggregations aggregations = searchHits.getAggregations();
        float maxScore = searchHits.getMaxScore();
        long totalHits = searchHits.getTotalHits();
        SearchHits.TotalHitsRelation totalHitsRelation = searchHits.getTotalHitsRelation();

        return new SearchHitsIterator<T>() {

            private volatile AtomicInteger currentCount = new AtomicInteger();
            private volatile Iterator<SearchHit<T>> currentScrollHits = searchHits.iterator();
            private volatile boolean continueScroll = currentScrollHits.hasNext();
            private volatile ScrollState scrollState = new ScrollState(searchHits.getScrollId());

            @Override
            public void close() {
                clearScrollConsumer.accept(scrollState.getScrollIds());
            }

            @Override
            @Nullable
            public Aggregations getAggregations() {
                return aggregations;
            }

            @Override
            public float getMaxScore() {
                return maxScore;
            }

            @Override
            public long getTotalHits() {
                return totalHits;
            }

            @Override
            public SearchHits.TotalHitsRelation getTotalHitsRelation() {
                return totalHitsRelation;
            }

            @Override
            public boolean hasNext() {

                if (!continueScroll || (maxCount > 0 && currentCount.get() >= maxCount)) {
                    return false;
                }

                if (!currentScrollHits.hasNext()) {
                    SearchHits<T> nextPage = continueScrollFunction.apply(scrollState.getScrollId());
                    currentScrollHits = nextPage.iterator();
                    scrollState.updateScrollId(nextPage.getScrollId());
                    continueScroll = currentScrollHits.hasNext();
                }

                return currentScrollHits.hasNext();
            }

            @Override
            public SearchHit<T> next() {
                if (hasNext()) {
                    currentCount.incrementAndGet();
                    return currentScrollHits.next();
                }
                throw new NoSuchElementException();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    // utility constructor
    private EsStreamQueries() {
    }
}
