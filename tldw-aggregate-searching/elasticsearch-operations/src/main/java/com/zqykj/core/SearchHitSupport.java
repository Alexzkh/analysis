/**
 * @作者 Mcj
 */
package com.zqykj.core;

import com.zqykj.core.aggregation.AggregatedPage;
import com.zqykj.core.aggregation.impl.AggregatedPageImpl;
import com.zqykj.domain.PageImpl;
import com.zqykj.domain.Pageable;
import com.zqykj.util.CloseableIterator;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <h1> 搜索支持 </h1>
 */
public class SearchHitSupport {

    private SearchHitSupport() {
    }

    /**
     * unwraps the data contained in a SearchHit for different types containing SearchHits if possible
     *
     * @param result the object, list, page or whatever containing SearchHit objects
     * @return a corresponding object where the SearchHits are replaced by their content if possible, otherwise the
     * original object
     */
    @Nullable
    public static Object unwrapSearchHits(@Nullable Object result) {

        if (result == null) {
            return result;
        }

        if (result instanceof SearchHit<?>) {
            return ((SearchHit<?>) result).getContent();
        }

        if (result instanceof List<?>) {
            return ((List<?>) result).stream() //
                    .map(SearchHitSupport::unwrapSearchHits) //
                    .collect(Collectors.toList());
        }

        if (result instanceof AggregatedPage<?>) {
            AggregatedPage<?> page = (AggregatedPage<?>) result;
            List<?> list = page.getContent().stream().map(SearchHitSupport::unwrapSearchHits).collect(Collectors.toList());
            return new AggregatedPageImpl<>(list, page.getPageable(), page.getTotalElements(), page.getAggregations(),
                    page.getScrollId(), page.getMaxScore());

        }

        if (result instanceof Stream<?>) {
            return ((Stream<?>) result).map(SearchHitSupport::unwrapSearchHits);
        }

        if (result instanceof SearchHits<?>) {
            SearchHits<?> searchHits = (SearchHits<?>) result;
            return unwrapSearchHits(searchHits.getSearchHits());
        }

        if (result instanceof SearchHitsIterator<?>) {
            return unwrapSearchHitsIterator((SearchHitsIterator<?>) result);
        }

        return result;
    }

    private static CloseableIterator<?> unwrapSearchHitsIterator(SearchHitsIterator<?> iterator) {

        return new CloseableIterator<Object>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Object next() {
                return unwrapSearchHits(iterator.next());
            }

            @Override
            public void close() {
                iterator.close();
            }
        };
    }

    /**
     * Builds an {@link AggregatedPage} with the {@link SearchHit} objects from a {@link SearchHits} object.
     *
     * @param searchHits, must not be {@literal null}.
     * @param pageable,   must not be {@literal null}.
     * @return the created Page
     * @deprecated since 4.0, will be removed in a future version.
     */
    @Deprecated
    public static <T> AggregatedPage<SearchHit<T>> page(SearchHits<T> searchHits, Pageable pageable) {
        return new AggregatedPageImpl<>( //
                searchHits.getSearchHits(), //
                pageable, //
                searchHits.getTotalHits(), //
                searchHits.getAggregations(), //
                null, //
                searchHits.getMaxScore());
    }

    public static <T> SearchPage<T> searchPageFor(SearchHits<T> searchHits, @Nullable Pageable pageable) {
        return new SearchPageImpl<>(searchHits, (pageable != null) ? pageable : Pageable.unpaged());
    }

    /**
     * SearchPage implementation.
     *
     * @param <T>
     */
    static class SearchPageImpl<T> extends PageImpl<SearchHit<T>> implements SearchPage<T> {

        private final SearchHits<T> searchHits;

        public SearchPageImpl(SearchHits<T> searchHits, Pageable pageable) {
            super(searchHits.getSearchHits(), pageable, searchHits.getTotalHits());
            this.searchHits = searchHits;
        }

        @Override
        public SearchHits<T> getSearchHits() {
            return searchHits;
        }

        /**
         * <h2> return the same instance as in getSearchHits().getSearchHits() </h2>
         */
        @Override
        public List<SearchHit<T>> getContent() {
            return searchHits.getSearchHits();
        }
    }
}
