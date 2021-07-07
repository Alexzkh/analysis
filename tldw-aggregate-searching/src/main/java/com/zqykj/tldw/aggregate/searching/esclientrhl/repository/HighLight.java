package com.zqykj.tldw.aggregate.searching.esclientrhl.repository;

import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Highlight object encapsulation
 **/
public class HighLight {
    /**
     * Highlight front label
     **/
    private String preTag = "";

    /**
     * Highlight post label
     **/
    private String postTag = "";

    /**
     * Highlight list
     **/
    private List<String> highLightList = null;

    /**
     * A builder for search highlighting.
     **/
    private HighlightBuilder highlightBuilder = null;

    public HighLight() {
        highLightList = new ArrayList<>();
    }

    public HighLight field(String fieldValue) {
        highLightList.add(fieldValue);
        return this;
    }

    public List<String> getHighLightList() {
        return highLightList;
    }

    public String getPreTag() {
        return preTag;
    }

    public void setPreTag(String preTag) {
        this.preTag = preTag;
    }

    public String getPostTag() {
        return postTag;
    }

    public void setPostTag(String postTag) {
        this.postTag = postTag;
    }

    public HighlightBuilder getHighlightBuilder() {
        return highlightBuilder;
    }

    public void setHighlightBuilder(HighlightBuilder highlightBuilder) {
        this.highlightBuilder = highlightBuilder;
    }
}
