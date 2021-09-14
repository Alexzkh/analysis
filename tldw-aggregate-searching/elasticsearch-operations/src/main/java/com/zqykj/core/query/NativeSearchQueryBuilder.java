/**
 * @author Mcj
 */
package com.zqykj.core.query;

import com.zqykj.domain.Pageable;
import com.zqykj.repository.query.IndexBoost;
import com.zqykj.repository.query.NativeSearchQuery;
import com.zqykj.repository.query.ScriptField;
import com.zqykj.repository.query.SourceFilter;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.collapse.CollapseBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.springframework.util.CollectionUtils.isEmpty;

public class NativeSearchQueryBuilder {

    @Nullable
    private QueryBuilder queryBuilder;
    @Nullable private QueryBuilder filterBuilder;
    private final List<ScriptField> scriptFields = new ArrayList<>();
    private final List<SortBuilder<?>> sortBuilders = new ArrayList<>();
    private final List<AbstractAggregationBuilder<?>> aggregationBuilders = new ArrayList<>();
    @Nullable private HighlightBuilder highlightBuilder;
    @Nullable private HighlightBuilder.Field[] highlightFields;
    private Pageable pageable = Pageable.unpaged();
    @Nullable private String[] fields;
    @Nullable private SourceFilter sourceFilter;
    @Nullable private CollapseBuilder collapseBuilder;
    @Nullable private List<IndexBoost> indicesBoost;
    private float minScore;
    private boolean trackScores;
    @Nullable private Collection<String> ids;
    @Nullable private String route;
    @Nullable private SearchType searchType;
    @Nullable private IndicesOptions indicesOptions;
    @Nullable private String preference;

    public NativeSearchQueryBuilder withQuery(QueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
        return this;
    }

    public NativeSearchQueryBuilder withFilter(QueryBuilder filterBuilder) {
        this.filterBuilder = filterBuilder;
        return this;
    }

    public NativeSearchQueryBuilder withSort(SortBuilder<?> sortBuilder) {
        this.sortBuilders.add(sortBuilder);
        return this;
    }

    public NativeSearchQueryBuilder withScriptField(ScriptField scriptField) {
        this.scriptFields.add(scriptField);
        return this;
    }

    public NativeSearchQueryBuilder withCollapseField(String collapseField) {
        this.collapseBuilder = new CollapseBuilder(collapseField);
        return this;
    }

    public NativeSearchQueryBuilder addAggregation(AbstractAggregationBuilder<?> aggregationBuilder) {
        this.aggregationBuilders.add(aggregationBuilder);
        return this;
    }

    public NativeSearchQueryBuilder withHighlightBuilder(HighlightBuilder highlightBuilder) {
        this.highlightBuilder = highlightBuilder;
        return this;
    }

    public NativeSearchQueryBuilder withHighlightFields(HighlightBuilder.Field... highlightFields) {
        this.highlightFields = highlightFields;
        return this;
    }

    public NativeSearchQueryBuilder withIndicesBoost(List<IndexBoost> indicesBoost) {
        this.indicesBoost = indicesBoost;
        return this;
    }

    public NativeSearchQueryBuilder withPageable(Pageable pageable) {
        this.pageable = pageable;
        return this;
    }

    public NativeSearchQueryBuilder withFields(String... fields) {
        this.fields = fields;
        return this;
    }

    public NativeSearchQueryBuilder withSourceFilter(SourceFilter sourceFilter) {
        this.sourceFilter = sourceFilter;
        return this;
    }

    public NativeSearchQueryBuilder withMinScore(float minScore) {
        this.minScore = minScore;
        return this;
    }

    /**
     * @param trackScores whether to track scores.
     * @return this object
     * @since 3.1
     */
    public NativeSearchQueryBuilder withTrackScores(boolean trackScores) {
        this.trackScores = trackScores;
        return this;
    }

    public NativeSearchQueryBuilder withIds(Collection<String> ids) {
        this.ids = ids;
        return this;
    }

    public NativeSearchQueryBuilder withRoute(String route) {
        this.route = route;
        return this;
    }

    public NativeSearchQueryBuilder withSearchType(SearchType searchType) {
        this.searchType = searchType;
        return this;
    }

    public NativeSearchQueryBuilder withIndicesOptions(IndicesOptions indicesOptions) {
        this.indicesOptions = indicesOptions;
        return this;
    }

    public NativeSearchQueryBuilder withPreference(String preference) {
        this.preference = preference;
        return this;
    }

    public NativeSearchQuery build() {

        NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(queryBuilder, filterBuilder, sortBuilders,
                highlightBuilder, highlightFields);

        nativeSearchQuery.setPageable(pageable);
        nativeSearchQuery.setTrackScores(trackScores);

        if (fields != null) {
            nativeSearchQuery.addFields(fields);
        }

        if (sourceFilter != null) {
            nativeSearchQuery.addSourceFilter(sourceFilter);
        }

        if (indicesBoost != null) {
            nativeSearchQuery.setIndicesBoost(indicesBoost);
        }

        if (!isEmpty(scriptFields)) {
            nativeSearchQuery.setScriptFields(scriptFields);
        }

        if (collapseBuilder != null) {
            nativeSearchQuery.setCollapseBuilder(collapseBuilder);
        }

        if (!isEmpty(aggregationBuilders)) {
            nativeSearchQuery.setAggregations(aggregationBuilders);
        }

        if (minScore > 0) {
            nativeSearchQuery.setMinScore(minScore);
        }

        if (ids != null) {
            nativeSearchQuery.setIds(ids);
        }

        if (route != null) {
            nativeSearchQuery.setRoute(route);
        }

        if (searchType != null) {
            nativeSearchQuery.setSearchType(searchType);
        }

        if (indicesOptions != null) {
            nativeSearchQuery.setIndicesOptions(indicesOptions);
        }
        if (preference != null) {
            nativeSearchQuery.setPreference(preference);
        }

        return nativeSearchQuery;
    }
}
