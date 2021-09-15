/**
 * @作者 Mcj
 */
package com.zqykj.repository.query;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.collapse.CollapseBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <h1> 本地查询: 包装了QueryBuilder 和一些其他参数 </h1>
 */
public class NativeSearchQuery extends AbstractQuery {

    @Nullable
    private final QueryBuilder query;
    @Nullable
    private QueryBuilder filter;
    @Nullable
    private List<SortBuilder<?>> sorts;
    private final List<ScriptField> scriptFields = new ArrayList<>();
    @Nullable
    private CollapseBuilder collapseBuilder;
    @Nullable
    private List<AbstractAggregationBuilder<?>> aggregations;
    @Nullable
    private HighlightBuilder highlightBuilder;
    @Nullable
    private HighlightBuilder.Field[] highlightFields;
    @Nullable
    private List<IndexBoost> indicesBoost;

    public NativeSearchQuery(@Nullable QueryBuilder query) {

        this.query = query;
    }

    public NativeSearchQuery(@Nullable QueryBuilder query, @Nullable QueryBuilder filter) {

        this.query = query;
        this.filter = filter;
    }

    public NativeSearchQuery(@Nullable QueryBuilder query, @Nullable QueryBuilder filter,
                             @Nullable List<SortBuilder<?>> sorts) {

        this.query = query;
        this.filter = filter;
        this.sorts = sorts;
    }

    public NativeSearchQuery(@Nullable QueryBuilder query, @Nullable QueryBuilder filter,
                             @Nullable List<SortBuilder<?>> sorts, @Nullable HighlightBuilder.Field[] highlightFields) {

        this.query = query;
        this.filter = filter;
        this.sorts = sorts;
        this.highlightFields = highlightFields;
    }

    public NativeSearchQuery(@Nullable QueryBuilder query, @Nullable QueryBuilder filter,
                             @Nullable List<SortBuilder<?>> sorts, @Nullable HighlightBuilder highlightBuilder,
                             @Nullable HighlightBuilder.Field[] highlightFields) {

        this.query = query;
        this.filter = filter;
        this.sorts = sorts;
        this.highlightBuilder = highlightBuilder;
        this.highlightFields = highlightFields;
    }

    @Nullable
    public QueryBuilder getQuery() {
        return query;
    }

    @Nullable
    public QueryBuilder getFilter() {
        return filter;
    }

    @Nullable
    public List<SortBuilder<?>> getElasticsearchSorts() {
        return sorts;
    }

    @Nullable
    public HighlightBuilder getHighlightBuilder() {
        return highlightBuilder;
    }

    @Nullable
    public HighlightBuilder.Field[] getHighlightFields() {
        return highlightFields;
    }

    public List<ScriptField> getScriptFields() {
        return scriptFields;
    }

    public void setScriptFields(List<ScriptField> scriptFields) {
        this.scriptFields.addAll(scriptFields);
    }

    public void addScriptField(ScriptField... scriptField) {
        scriptFields.addAll(Arrays.asList(scriptField));
    }

    @Nullable
    public CollapseBuilder getCollapseBuilder() {
        return collapseBuilder;
    }

    public void setCollapseBuilder(CollapseBuilder collapseBuilder) {
        this.collapseBuilder = collapseBuilder;
    }

    @Nullable
    public List<AbstractAggregationBuilder<?>> getAggregations() {
        return aggregations;
    }

    public void addAggregation(AbstractAggregationBuilder<?> aggregationBuilder) {

        if (aggregations == null) {
            aggregations = new ArrayList<>();
        }

        aggregations.add(aggregationBuilder);
    }

    public void setAggregations(List<AbstractAggregationBuilder<?>> aggregations) {
        this.aggregations = aggregations;
    }

    @Nullable
    public List<IndexBoost> getIndicesBoost() {
        return indicesBoost;
    }

    public void setIndicesBoost(List<IndexBoost> indicesBoost) {
        this.indicesBoost = indicesBoost;
    }

}
