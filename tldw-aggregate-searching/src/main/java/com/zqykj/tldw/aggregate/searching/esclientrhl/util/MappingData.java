package com.zqykj.tldw.aggregate.searching.esclientrhl.util;

/**
 * Mapping annotation corresponding data carrier class
 **/
public class MappingData {

    String field_name;
    /**
     * Data type (including keyword type)
     */
    String datatype;
    /**
     * Indirect keyword
     */
    boolean keyword;

    /**
     * Keywords ignore words
     */
    int ignore_above;
    /**
     * Whether to support NGram, efficient full text search prompt
     */
    boolean ngram;
    /**
     * Do you support suggest and efficient prefix search
     */
    boolean suggest;
    /**
     * Index word splitter settings
     */
    String analyzer;
    /**
     * Search content word splitter settings
     */
    String search_analyzer;


    private boolean allow_search;

    private String copy_to;

    private String null_value;

    private Class nested_class;


    public String getField_name() {
        return field_name;
    }

    public void setField_name(String field_name) {
        this.field_name = field_name;
    }

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public boolean isKeyword() {
        return keyword;
    }

    public void setKeyword(boolean keyword) {
        this.keyword = keyword;
    }

    public int getIgnore_above() {
        return ignore_above;
    }

    public void setIgnore_above(int ignore_above) {
        this.ignore_above = ignore_above;
    }

    public boolean isNgram() {
        return ngram;
    }

    public void setNgram(boolean ngram) {
        this.ngram = ngram;
    }

    public boolean isSuggest() {
        return suggest;
    }

    public void setSuggest(boolean suggest) {
        this.suggest = suggest;
    }

    public String getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(String analyzer) {
        this.analyzer = analyzer;
    }

    public String getSearch_analyzer() {
        return search_analyzer;
    }

    public void setSearch_analyzer(String search_analyzer) {
        this.search_analyzer = search_analyzer;
    }

//    public String getAnalyzedtype() {
//        return analyzedtype;
//    }
//
//    public void setAnalyzedtype(String analyzedtype) {
//        this.analyzedtype = analyzedtype;
//    }


    public String getCopy_to() {
        return copy_to;
    }

    public void setCopy_to(String copy_to) {
        this.copy_to = copy_to;
    }

    public boolean isAllow_search() {
        return allow_search;
    }

    public void setAllow_search(boolean allow_search) {
        this.allow_search = allow_search;
    }

    public String getNull_value() {
        return null_value;
    }

    public void setNull_value(String null_value) {
        this.null_value = null_value;
    }

    public Class getNested_class() {
        return nested_class;
    }

    public void setNested_class(Class nested_class) {
        this.nested_class = nested_class;
    }
}
