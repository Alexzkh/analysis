package com.zqykj.tldw.aggregate;

import com.zqykj.annotations.NoRepositoryBean;

import java.util.Optional;

/**
 * @param <T> the domain type .
 * @param <M> the type of id of the domain entity .
 * @Description: The basic data operations.
 * <p>
 * The implementions or successors can be mongodb 、solr 、elasticsearch even hbase.
 * you can also do something special in your operations what the implementions of this.
 * <p>
 * @Author zhangkehou
 * @Date 2021/8/5
 */
@NoRepositoryBean
public interface CRUDOperations<T, M> extends BaseOperations<T,M> {

    enum DatasoureType {
        Elasticsearch,

        MongoDB,

        Solr,

        HBase
    }


    /**
     * insert data
     * @param t data
     */
    public boolean save(T t) throws Exception;


    /**
     * Remove all data that match the key of id from the table used to store the entity class.
     *
     * @return long the number of documents (collections、indexes..)deleted .
     */
    boolean deleteById(M id) throws Exception;


    /**
     * Search all data that match the key of id from the table used to store the entity class.
     *
     * @param id   the key of id .
     * @return the converted object or null if the result does not exist .
     */
    Optional<T> findById(M id) throws Exception;



    /**
     * Start creating an update operation for the given id and domainType .
     *
     * @param id     the key of id .
     * @param name   the operate table name .
     */
    boolean updateByID(M id, String name) throws Exception;




}





