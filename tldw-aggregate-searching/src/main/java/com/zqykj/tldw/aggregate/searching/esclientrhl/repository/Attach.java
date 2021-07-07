package com.zqykj.tldw.aggregate.searching.esclientrhl.repository;

/**
 * Provide stronger query function and optional conditions
 **/
public class Attach {
    /**
     * page 、sort、highlight param.
     **/
    private PageSortHighLight pageSortHighLight = null;

    /**
     * Return to field filter -- includes.
     **/
    private String[] includes;

    /**
     * Return to field filter --exclude.
     **/
    private String[] excludes;

    /**
     * When you index a document, you can specify an optional routing value, which routes the document to a specific shard.
     **/
    private String routing;

    /**
     *<p>
     * search_ After is not a free jump to random pages, but a solution to scroll multiple queries in parallel.
     *It's very similar to the rolling API, but different from it, search_after parameter is stateless and always resolves to the latest version of the searcher.
     *When we use search_after parameter, the from parameter must be set to 0 or - 1 (of course, you may not set the from parameter).
     * </p>
     **/
    private boolean searchAfter = false;

    /**
     * <p>
     *Generally the total hit count can't be computed accurately without visiting all matches, which is costly for queries that match lots of documents.
     *The track_total_hits parameter allows you to control how the total number of hits should be tracked.
     * </p>
     * @see {https://www.elastic.co/guide/en/elasticsearch/reference/current/search-your-data.html#track-total-hits}
     **/
    private boolean trackTotalHits = false;

    /**
     * sort field
     **/
    private Object[] sortValues;

    public String[] getIncludes() {
        return includes;
    }

    public void setIncludes(String[] includes) {
        this.includes = includes;
    }

    public String[] getExcludes() {
        return excludes;
    }

    public void setExcludes(String[] excludes) {
        this.excludes = excludes;
    }

    public PageSortHighLight getPageSortHighLight() {
        return pageSortHighLight;
    }

    public void setPageSortHighLight(PageSortHighLight pageSortHighLight) {
        this.pageSortHighLight = pageSortHighLight;
    }

    public String getRouting() {
        return routing;
    }

    public void setRouting(String routing) {
        this.routing = routing;
    }

    public boolean isSearchAfter() {
        return searchAfter;
    }

    public void setSearchAfter(boolean searchAfter) {
        this.searchAfter = searchAfter;
    }

    public Object[] getSortValues() {
        return sortValues;
    }

    public void setSortValues(Object[] sortValues) {
        this.sortValues = sortValues;
    }

    public boolean isTrackTotalHits() {
        return trackTotalHits;
    }

    public void setTrackTotalHits(boolean trackTotalHits) {
        this.trackTotalHits = trackTotalHits;
    }
}
