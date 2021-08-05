/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.data.query;

import com.zqykj.tldw.aggregate.domian.Pageable;
import com.zqykj.tldw.aggregate.domian.Sort;
import lombok.Getter;
import lombok.Setter;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.support.IndicesOptions;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.addAll;

@Setter
@Getter
public class ElasticsearchStringQuery implements Query {

    protected Pageable pageable = DEFAULT_PAGE;
    @Nullable
    protected Sort sort;
    protected List<String> fields = new ArrayList<>();
    protected float minScore;
    @Nullable
    protected Collection<String> ids;
    @Nullable
    protected String route;
    protected SearchType searchType = SearchType.DFS_QUERY_THEN_FETCH;
    @Nullable
    protected IndicesOptions indicesOptions;
    protected boolean trackScores;
    @Nullable
    protected String preference;
    @Nullable
    protected Integer maxResults;
    @Nullable
    protected HighlightQuery highlightQuery;
    @Nullable
    private Boolean trackTotalHits;
    @Nullable
    private Integer trackTotalHitsUpTo;
    @Nullable
    private Duration scrollTime;
    private String source;

    public ElasticsearchStringQuery(String source) {
        this.source = source;
    }

    public ElasticsearchStringQuery(String source, Pageable pageable) {
        this.source = source;
        this.pageable = pageable;
    }

    public ElasticsearchStringQuery(String source, Pageable pageable, Sort sort) {
        this.source = source;
        this.pageable = pageable;
        this.sort = sort;
    }

    @Override
    public <T extends Query> T setPageable(Pageable pageable) {
        Assert.notNull(pageable, "Pageable must not be null!");

        this.pageable = pageable;
        return (T) this.addSort(pageable.getSort());
    }

    @Override
    public Pageable getPageable() {
        return this.pageable;
    }

    @Override
    @Nullable
    public Sort getSort() {
        return this.sort;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <T extends Query> T addSort(Sort sort) {
        if (sort == null) {
            return (T) this;
        }

        if (this.sort == null) {
            this.sort = sort;
        } else {
            this.sort = this.sort.and(sort);
        }

        return (T) this;
    }

    public void addFields(String... fields) {
        addAll(this.fields, fields);
    }
}
