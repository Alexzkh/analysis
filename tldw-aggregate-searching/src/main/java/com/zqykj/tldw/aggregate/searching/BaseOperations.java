package com.zqykj.tldw.aggregate.searching;

import java.util.Collection;
import java.util.List;
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
public interface BaseOperations<T, M> {

    enum DatasoureType {
        Elasticsearch,

        MongoDB,

        Solr,

        HBase
    }

    /**
     * Save the object to the collection for the entity type of the object to save.
     *
     * @param clazz the object to store in the collection. Must not be null.
     * @return none .
     */
    <T> void create(T clazz) throws Exception;


    /**
     * Remove all data that match the key of id from the table used to store the entity class.
     *
     * @param id    the key of id .
     * @param clazz the operate entity .
     * @return long the number of documents (collections、indexes..)deleted .
     */
    boolean deleteByID(M id, Class<T> clazz) throws Exception;


    /**
     * Search all data that match the key of id from the table used to store the entity class.
     *
     * @param id   the key of id .
     * @param clazz the operate entity.
     * @return the converted object or null if the result does not exist .
     */
    Optional<T> findById(M id, Class<T> clazz) throws Exception;



    /**
     * Start creating an update operation for the given id and domainType .
     *
     * @param id     the key of id .
     * @param entity the given domain type ,must not be null.
     * @param name   the operate table name .
     */
    boolean updateByID(M id, T entity, String name) throws Exception;


    /**
     * Start creating an update operation for the given domainType.
     *
     * @param entity the given domain type , must not be null.
     * @param name   the operate table name .
     */
    boolean update(T entity, String name) throws Exception;


}





