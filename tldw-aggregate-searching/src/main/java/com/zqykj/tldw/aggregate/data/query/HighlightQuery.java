/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.data.query;

import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;

/**
 * <h1> 高亮查询定义 </h1>
 */
public class HighlightQuery {

    private final HighlightBuilder highlightBuilder;


    public HighlightQuery(HighlightBuilder highlightBuilder) {
        this.highlightBuilder = highlightBuilder;
    }

    public HighlightBuilder getHighlightBuilder() {
        return highlightBuilder;
    }
}
