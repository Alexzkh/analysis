package com.zqykj.tldw.aggregate.searching.esclientrhl;

import com.zqykj.annotations.NoRepositoryBean;
import com.zqykj.tldw.aggregate.CRUDOperations;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;

import java.util.List;


/**
 * Elasticsearch basic function components
 *
 * @see {https://www.elastic.co/guide/en/elasticsearch/reference/7.9/getting-started.html}
 **/
@SuppressWarnings("unchecked")
@NoRepositoryBean
public interface ElasticsearchOperations<T, M> extends CRUDOperations<T, M> {


    /**
     * description：Create index for specified route
     *
     * @param t        domain data
     * @param routing: specified route
     * @return: boolean
     **/
//    public boolean save(T t, String routing) throws Exception;

    /**
     * Non paged query
     *
     * @param queryBuilder query contion
     * @return
     * @throws Exception
     */
    public List<T> search(QueryBuilder queryBuilder) throws Exception;

    /**
     * Non paged query(Cross index)
     *
     * @param queryBuilder query contion
     * @param indexs       index name
     * @return
     * @throws Exception
     */
    public List<T> search(QueryBuilder queryBuilder, String... indexs) throws Exception;


    /**
     * delete index (routing)
     *
     * @param id      _id
     * @param routing routing information (_id is the default routing )
     * @return
     * @throws Exception
     */
    public boolean delete(M id, String routing) throws Exception;


    /**
     * Delte index by condition
     * https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high-document-delete-by-query.html#java-rest-high-document-delete-by-query-response
     *
     * @param queryBuilder query condition
     * @return
     * @throws Exception
     */
    public BulkByScrollResponse deleteByCondition(QueryBuilder queryBuilder) throws Exception;


    /**
     * Multi get data by ids
     *
     * @param ids ids array
     * @return
     * @throws Exception
     */
    public List<T> multiGetById(M[] ids) throws Exception;


    /**
     * save index collection
     *
     * @param list index pojo collection
     */
//    public BulkResponse save(List<T> list) throws Exception;
}
