/**
 * @作者 Mcj
 */
package com.zqykj.core.aggregation.impl;

import com.zqykj.core.aggregation.AggregatedPage;
import com.zqykj.core.document.SearchDocumentResponse;
import com.zqykj.domain.PageImpl;
import com.zqykj.domain.Pageable;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.springframework.lang.Nullable;

import java.util.List;

import static java.util.Optional.ofNullable;

public class AggregatedPageImpl<T> extends PageImpl<T> implements AggregatedPage<T> {

    @Nullable
    private Aggregations aggregations;
    @Nullable
    private String scrollId;
    private float maxScore;

    private static Pageable pageableOrUnpaged(@Nullable Pageable pageable) {
        return ofNullable(pageable).orElse(Pageable.unpaged());
    }

    public AggregatedPageImpl(List<T> content) {
        super(content);
    }

    public AggregatedPageImpl(List<T> content, float maxScore) {
        super(content);
        this.maxScore = maxScore;
    }

    public AggregatedPageImpl(List<T> content, String scrollId) {
        super(content);
        this.scrollId = scrollId;
    }

    public AggregatedPageImpl(List<T> content, String scrollId, float maxScore) {
        this(content, scrollId);
        this.maxScore = maxScore;
    }

    public AggregatedPageImpl(List<T> content, Pageable pageable, long total) {
        super(content, pageableOrUnpaged(pageable), total);
    }

    public AggregatedPageImpl(List<T> content, Pageable pageable, long total, float maxScore) {
        super(content, pageableOrUnpaged(pageable), total);
        this.maxScore = maxScore;
    }

    public AggregatedPageImpl(List<T> content, Pageable pageable, long total, String scrollId) {
        super(content, pageableOrUnpaged(pageable), total);
        this.scrollId = scrollId;
    }

    public AggregatedPageImpl(List<T> content, Pageable pageable, long total, String scrollId, float maxScore) {
        this(content, pageableOrUnpaged(pageable), total, scrollId);
        this.maxScore = maxScore;
    }

    public AggregatedPageImpl(List<T> content, Pageable pageable, long total, @Nullable Aggregations aggregations) {
        super(content, pageableOrUnpaged(pageable), total);
        this.aggregations = aggregations;
    }

    public AggregatedPageImpl(List<T> content, Pageable pageable, long total, @Nullable Aggregations aggregations,
                              float maxScore) {
        this(content, pageableOrUnpaged(pageable), total, aggregations);
        this.maxScore = maxScore;
    }

    public AggregatedPageImpl(List<T> content, Pageable pageable, long total, @Nullable Aggregations aggregations,
                              String scrollId) {
        this(content, pageableOrUnpaged(pageable), total, aggregations);
        this.scrollId = scrollId;
    }

    public AggregatedPageImpl(List<T> content, Pageable pageable, long total, @Nullable Aggregations aggregations,
                              String scrollId, float maxScore) {
        this(content, pageableOrUnpaged(pageable), total, aggregations, scrollId);
        this.maxScore = maxScore;
    }

    public AggregatedPageImpl(List<T> content, Pageable pageable, SearchDocumentResponse response) {
        this(content, pageableOrUnpaged(pageable), response.getTotalHits(), response.getAggregations(),
                response.getScrollId(), response.getMaxScore());
    }

    @Override
    public boolean hasAggregations() {
        return aggregations != null;
    }

    @Override
    @Nullable
    public Aggregations getAggregations() {
        return aggregations;
    }

    @Override
    @Nullable
    public Aggregation getAggregation(String name) {
        return aggregations == null ? null : aggregations.get(name);
    }

    @Nullable
    @Override
    public String getScrollId() {
        return scrollId;
    }

    @Override
    public float getMaxScore() {
        return maxScore;
    }
}
