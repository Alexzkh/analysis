package com.zqykj.tldw.aggregate.searching.esclientrhl.repository;

/**
 * Pagination + sorting + highlight object encapsulation
 **/
public class PageSortHighLight {
    /**
     * current page.
     **/
    private int currentPage;

    /**
     *page size.
     **/
    private int pageSize;
    /**
     * sort.
     **/
    Sort sort = new Sort();

    /**
     * Highlight object.
     **/
    private HighLight highLight = new HighLight();

    public PageSortHighLight(int currentPage, int pageSize) {
        this.currentPage = currentPage;
        this.pageSize = pageSize;
    }

    public PageSortHighLight(int currentPage, int pageSize, Sort sort) {
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.sort = sort;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    public HighLight getHighLight() {
        return highLight;
    }

    public void setHighLight(HighLight highLight) {
        this.highLight = highLight;
    }

}
