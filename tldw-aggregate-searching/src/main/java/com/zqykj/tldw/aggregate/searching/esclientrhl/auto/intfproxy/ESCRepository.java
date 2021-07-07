package com.zqykj.tldw.aggregate.searching.esclientrhl.auto.intfproxy;

import com.zqykj.tldw.aggregate.searching.esclientrhl.enums.AggsType;
import com.zqykj.tldw.aggregate.searching.esclientrhl.repository.PageList;
import com.zqykj.tldw.aggregate.searching.esclientrhl.repository.PageSortHighLight;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.index.query.QueryBuilder;

import java.util.List;
import java.util.Map;

/**
 * description:
 **/
public interface ESCRepository<T, M> {

    /**
     * Query by low level rest client.
     *
     * @param request
     * @return Response
     * @throws Exception
     */
    public Response request(Request request) throws Exception;


    /**
     * Create index.
     *
     * @param t
     */
    public boolean save(T t) throws Exception;


    /**
     * Bulk create index.
     *
     * @param list
     */
    public BulkResponse save(List<T> list) throws Exception;

    /**
     * Update index by value field.
     *
     * @param t
     */
    public boolean update(T t) throws Exception;


    /**
     * Updete cover index.
     *
     * @param t
     */
    public boolean updateCover(T t) throws Exception;


    /**
     * Delete index.
     *
     * @param t
     */
    public boolean delete(T t) throws Exception;

    /**
     * Delete index.
     *
     * @param id
     */
    public boolean deleteById(M id) throws Exception;


    /**
     * Query by id.
     *
     * @param id
     * @return
     * @throws Exception
     */
    public T getById(M id) throws Exception;


    /**
     * The most original query .
     *
     * @param searchRequest
     * @return
     * @throws Exception
     */
    public SearchResponse search(SearchRequest searchRequest) throws Exception;


    /**
     * Query without pageable.
     *
     * @param queryBuilder
     * @return
     * @throws Exception
     */
    public List<T> search(QueryBuilder queryBuilder) throws Exception;


    /**
     * count
     *
     * @param queryBuilder
     * @return
     * @throws Exception
     */
    public long count(QueryBuilder queryBuilder) throws Exception;


    /**
     * pageable、highlight、sort query
     *
     * @param queryBuilder
     * @param pageSortHighLight
     * @return
     * @throws Exception
     */
    public PageList<T> search(QueryBuilder queryBuilder, PageSortHighLight pageSortHighLight) throws Exception;


    /**
     * Non paged query, specify the maximum number of returns
     *
     * @param queryBuilder
     * @param limitSize    the maximum number of returns
     * @return
     * @throws Exception
     */
    public List<T> searchMore(QueryBuilder queryBuilder, int limitSize) throws Exception;


    /**
     * search suggest.
     *
     * @param fieldName
     * @param fieldValue
     * @return
     * @throws Exception
     */
    public List<String> completionSuggest(String fieldName, String fieldValue) throws Exception;


    /**
     * General aggregate query by bucket group used by {@link AggsType} metric
     * 以bucket分组以aggstypes的方式metric度量
     *
     * @param bucketName
     * @param metricName
     * @param aggsType
     * @return
     */
    public Map aggs(String metricName, AggsType aggsType, QueryBuilder queryBuilder, String bucketName) throws Exception;

}
