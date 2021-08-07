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
    <T> void create(Class<T> clazz) throws Exception;


    /**
     * Remove all data that match the key of id from the table used to store the entity class.
     *
     * @param id   the key of id .
     * @param name the operate table name ,it might be the mongo collection name 、elasticsearch index name or solr document name 、 hbase table name .
     * @return long the number of documents (collections、indexes..)deleted .
     */
    long deleteByID(M id, String name) throws Exception;


    /**
     * Remove all data that match the key of id from the table used to store the entity class.
     *
     * @param ids  the number of keys of id .
     * @param name the operate table name ,it might be the mongo collection name 、elasticsearch index name or solr document name 、 hbase table name .
     * @return long the number of documents (collections、indexes..)deleted .
     */
    long batchDelteByID(Collection<M> ids, String name) throws Exception;


    /**
     * Search all data that match the key of id from the table used to store the entity class.
     *
     * @param id   the key of id .
     * @param name the operate table name ,it might be the mongo collection name 、elasticsearch index name or solr document name 、 hbase table name .
     * @return the converted object or null if the result does not exist .
     */
    Optional<T> findById(M id, String name) throws Exception;


    /**
     * Search all data that match the key of id from the table used to store the entity class.
     *
     * @param ids  the number of keys  of id .
     * @param name the operate table name ,it might be the mongo collection name 、elasticsearch index name or solr document name 、 hbase table name .
     * @return return the results in a list or  null if the results does not exist .
     */
    List<T> findAllByIDs(Collection<M> ids, String name) throws Exception;


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





