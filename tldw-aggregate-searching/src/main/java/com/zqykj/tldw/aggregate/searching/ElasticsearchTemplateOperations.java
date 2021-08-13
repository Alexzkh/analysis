package com.zqykj.tldw.aggregate.searching;

import com.zqykj.annotations.NoRepositoryBean;
import org.elasticsearch.index.query.QueryBuilder;

import java.util.List;


/**
 * Elasticsearch basic function components
 *
 * @see {https://www.elastic.co/guide/en/elasticsearch/reference/7.9/getting-started.html}
 **/
@SuppressWarnings("unchecked")
@NoRepositoryBean
public interface ElasticsearchTemplateOperations<T, M> extends BaseOperations<T, M> {
    /**
     * create index
     *
     * @param t index pojo
     */
    public boolean save(T t) throws Exception;


    /**
     * Non paged query
     *
     * @param queryBuilder query contion
     * @param clazz        index pojo class type
     * @return
     * @throws Exception
     */
    public List<T> search(QueryBuilder queryBuilder, Class<T> clazz) throws Exception;

    /**
     * Non paged query(Cross index)
     *
     * @param queryBuilder query contion
     * @param clazz        index pojo class type
     * @param indexs       index name
     * @return
     * @throws Exception
     */
    public List<T> search(QueryBuilder queryBuilder, Class<T> clazz, String... indexs) throws Exception;

}
