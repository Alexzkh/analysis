package com.zqykj.tldw.aggregate.searching.esclientrhl.repository;

import java.util.List;

/**
 * Pagination object encapsulation.
 **/
public class PageList<T> {
    /**
     * page result list .
     **/
    List<T> list;

    /**
     * total pages.
     **/
    private int totalPages = 0;

    /**
     * total elements .
     **/
    private long totalElements = 0;

    /**
     * sort values .
     **/
    private Object[] sortValues;

    /**
     * current page.
     **/
    private int currentPage;

    /**
     * page size.
     **/
    private int pageSize;

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public Object[] getSortValues() {
        return sortValues;
    }

    public void setSortValues(Object[] sortValues) {
        this.sortValues = sortValues;
    }
}
