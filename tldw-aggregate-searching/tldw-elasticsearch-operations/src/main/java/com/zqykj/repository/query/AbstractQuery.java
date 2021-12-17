/**
 * @作者 Mcj
 */
package com.zqykj.repository.query;

import com.zqykj.domain.Pageable;
import com.zqykj.domain.Sort;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.support.IndicesOptions;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.addAll;

/**
 * 本地一些搜索查询的顶级查询
 */
public class AbstractQuery implements Query {

    protected Pageable pageable = DEFAULT_PAGE;
    protected int from = 0;
    protected int size = 10_000;
    @Nullable
    protected Sort sort;
    protected List<String> fields = new ArrayList<>();
    protected List<String> excludeFields = new ArrayList<>();
    @Nullable
    protected SourceFilter sourceFilter;
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

    @Override
    @Nullable
    public Sort getSort() {
        return this.sort;
    }

    @Override
    public Pageable getPageable() {
        return this.pageable;
    }

    public int from() {
        return this.from;
    }

    public int size() {
        return this.size;
    }

    @Override
    public final <T extends Query> T setPageable(Pageable pageable) {

        Assert.notNull(pageable, "Pageable must not be null!");

        this.pageable = pageable;
        return (T) this.addSort(pageable.getSort());
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getFrom() {
        return from;
    }

    public int getSize() {
        return size;
    }

    @Override
    public void addFields(String... fields) {
        addAll(this.fields, fields);
    }

    @Override
    public void addExcludeFields(String... fields) {
        addAll(this.excludeFields, fields);
    }

    @Override
    public List<String> getFields() {
        return fields;
    }

    @Override
    public List<String> getExcludeFields() {
        return excludeFields;
    }

    @Override
    public void addSourceFilter(SourceFilter sourceFilter) {
        this.sourceFilter = sourceFilter;
    }

    @Nullable
    @Override
    public SourceFilter getSourceFilter() {
        return sourceFilter;
    }

    @Override
    @SuppressWarnings("unchecked")
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

    @Override
    public float getMinScore() {
        return minScore;
    }

    public void setMinScore(float minScore) {
        this.minScore = minScore;
    }

    @Nullable
    @Override
    public Collection<String> getIds() {
        return ids;
    }

    public void setIds(Collection<String> ids) {
        this.ids = ids;
    }

    @Nullable
    @Override
    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public void setSearchType(SearchType searchType) {
        this.searchType = searchType;
    }

    @Override
    public SearchType getSearchType() {
        return searchType;
    }

    @Nullable
    @Override
    public IndicesOptions getIndicesOptions() {
        return indicesOptions;
    }

    public void setIndicesOptions(IndicesOptions indicesOptions) {
        this.indicesOptions = indicesOptions;
    }

    @Override
    public boolean getTrackScores() {
        return trackScores;
    }

    /**
     * Configures whether to track scores.
     *
     * @param trackScores
     * @since 3.1
     */
    public void setTrackScores(boolean trackScores) {
        this.trackScores = trackScores;
    }

    @Nullable
    @Override
    public String getPreference() {
        return preference;
    }

    @Override
    public void setPreference(String preference) {
        this.preference = preference;
    }

    @Override
    public boolean isLimiting() {
        return maxResults != null;
    }

    @Nullable
    @Override
    public Integer getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
    }

    @Override
    public void setHighlightQuery(HighlightQuery highlightQuery) {
        this.highlightQuery = highlightQuery;
    }

    @Override
    public Optional<HighlightQuery> getHighlightQuery() {
        return Optional.ofNullable(highlightQuery);
    }

    @Override
    public void setTrackTotalHits(@Nullable Boolean trackTotalHits) {
        this.trackTotalHits = trackTotalHits;
    }

    @Override
    @Nullable
    public Boolean getTrackTotalHits() {
        return trackTotalHits;
    }

    @Override
    public void setTrackTotalHitsUpTo(@Nullable Integer trackTotalHitsUpTo) {
        this.trackTotalHitsUpTo = trackTotalHitsUpTo;
    }

    @Override
    @Nullable
    public Integer getTrackTotalHitsUpTo() {
        return trackTotalHitsUpTo;
    }

    @Nullable
    @Override
    public Duration getScrollTime() {
        return scrollTime;
    }

    @Override
    public void setScrollTime(@Nullable Duration scrollTime) {
        this.scrollTime = scrollTime;
    }
}
