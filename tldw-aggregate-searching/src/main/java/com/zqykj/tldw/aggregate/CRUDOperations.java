package com.zqykj.tldw.aggregate;

import com.zqykj.annotations.NoRepositoryBean;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Optional;

/**
 * Interface for generic CRUD operations on a operations for a specific type.
 *
 * @param <T> the domain type the operations manages
 * @param <M> the type of the id of the entity the operations manages
 * @Author zhangkehou
 * @Date 2021/8/17
 */
@NoRepositoryBean
public interface CRUDOperations<T, M> extends BaseOperations<T, M> {

    enum DatasoureType {
        Elasticsearch,

        MongoDB,

        Solr,

        HBase
    }

    <S extends T> S save(S entity);

    <S extends T> Iterable<S> saveAll(Iterable<S> entities);

    /**
     * Search all data that match the key of id from the table used to store the entity class.
     *
     * @param id the key of id .
     * @return the converted object or null if the result does not exist .
     */
    Optional<T> findById(M id, @NonNull String routing) throws Exception;

    /**
     * insert data
     * @param t data
     */
//    public boolean save(T t) throws exception;


    /**
     * Remove all data that match the key of id from the table used to store the entity class.
     *
     * @return long the number of documents (collections、indexes..)deleted .
     */
    boolean deleteById(M id) throws Exception;


    /**
     * Start creating an update operation for the given id and domainType .
     *
     * @param id   the key of id .
     * @param name the operate table name .
     */
    boolean updateByID(M id, String name) throws Exception;


}





