/**
 * @作者 Mcj
 */
package com.zqykj.core;

import com.zqykj.core.document.Document;
import com.zqykj.domain.Routing;
import com.zqykj.repository.query.*;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * <h1> Elasticsearch Document operations </h1>
 */
public interface DocumentOperations {

    @Nullable
    <T> T get(String id, Class<T> clazz, String routing);

    /**
     * <h2> 通过给定的索引 </h2>
     *
     * @param id      the id of the object
     * @param clazz   the entity class,
     * @param index   the index from which the object is read.
     * @param routing 指定路由 (也可以在entity 中 添加一个property {@link Routing}
     * @return the entity
     */
    @Nullable
    <T> T get(String id, Class<T> clazz, String index, String routing);

    /**
     * <h2> 将entity 保存到对应索引下 </h2>
     *
     * @param entity  the entity to save, must not be {@literal null}
     * @param <T>     the entity type
     * @param routing 指定路由 (也可以在entity 中 添加一个property {@link Routing}
     * @return the saved entity
     */
    <T> T save(T entity, String routing);


    /**
     * <h2> 将一组entity 保存到对应索引下(批量操作)  </h2>
     *
     * @param entities must not be {@literal null}
     * @param <T>      the entity type
     * @param routing  指定路由 (也可以在entity 中 添加一个property {@link Routing}
     * @return the saved entities
     */
    <T> Iterable<T> save(Iterable<T> entities, String routing);

    /**
     * <h2> 通过给定索引保存 entities </h2>
     *
     * @param entities must not be {@literal null}
     * @param index    the index to save the entities in, must not be {@literal null}
     * @param <T>      the entity type
     * @param routing  指定路由 (也可以在entity 中 添加一个property {@link Routing}
     * @return the saved entities
     */
    <T> Iterable<T> save(Iterable<T> entities, String index, String routing);

    /**
     * <h2> 批量索引操作 </h2>
     */
    List<IndexedObjectInformation> bulkIndex(List<IndexQuery> queries, Class<?> clazz);

    default List<IndexedObjectInformation> bulkIndex(List<IndexQuery> queries, String index) {
        return bulkIndex(queries, BulkOptions.defaultOptions(), index);
    }

    /**
     * <h2> 批量索引操作 </h2>
     */
    List<IndexedObjectInformation> bulkIndex(List<IndexQuery> queries, BulkOptions bulkOptions, String index);

    /**
     * <h2> 文档的部分更新 </h2>
     *
     * @param updateQuery query defining the update
     * @param index       the index where to update the records
     * @return the update response
     */
    UpdateResponse update(UpdateQuery updateQuery, String index);

    default void bulkUpdate(List<UpdateQuery> queries, String index) {
        bulkUpdate(queries, BulkOptions.defaultOptions(), index);
    }

    /**
     * <h2> 批量更新所有对象, 会做更新 </h2>
     *
     * @param clazz   the entity class
     * @param queries the queries to execute in bulk
     * @since 4.1
     */
    void bulkUpdate(List<UpdateQuery> queries, Class<?> clazz);

    /**
     * <h2> 批量更新所有对象, 会做更新 </h2>
     *
     * @param queries     the queries to execute in bulk
     * @param bulkOptions options to be added to the bulk request
     */
    void bulkUpdate(List<UpdateQuery> queries, BulkOptions bulkOptions, String index);

    /**
     * <h2> 根据Id 与 路由 删除一个对象 </h2>
     *
     * @param id      文档Id
     * @param routing 指定路由 (也可以在entity 中 添加一个property {@link Routing}
     * @param index   索引名称
     */
    String delete(String id, String routing, String index);

    /**
     * <h2> 删除匹配查询的所有数据 </h2>
     *
     * @param query query defining the objects
     * @param clazz The entity class, must be annotated with
     *              {@link Document}
     * @param index 索引名称
     * @since 4.1
     */
    void delete(Query query, Class<?> clazz, String index);

    /**
     * <h2> 返回给定查询匹配到数据总数 </h2>
     *
     * @param query the query to execute
     * @param clazz the entity clazz used for property mapping and index name extraction
     * @return count
     */
    long count(Query query, Class<?> clazz);

    /**
     * <h2> 返回给定查询匹配到数据总数 </h2>
     *
     * @param query the query to execute
     * @param clazz the entity clazz used for property mapping
     * @param index the index to run the query against
     * @return count
     */
    long count(Query query, @Nullable Class<?> clazz, String index);

    @Nullable
    default String stringIdRepresentation(@Nullable Object id) {
        return Objects.toString(id, null);
    }

    <T> List<T> multiGet(Query query, Class<T> clazz);

    <T> List<T> multiGet(Query query, Class<T> clazz, String index);
}
