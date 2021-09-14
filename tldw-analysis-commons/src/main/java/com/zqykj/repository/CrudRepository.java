package com.zqykj.repository;

import com.zqykj.annotations.NoRepositoryBean;
import com.zqykj.domain.Page;
import com.zqykj.domain.Pageable;
import com.zqykj.domain.Routing;

import java.util.Optional;

/**
 * Interface for generic CRUD operations on a operations for a specific type.
 *
 * @param <T>  the domain type the operations manages
 * @param <ID> the type of the id of the entity the operations manages
 * @Author zhangkehou
 * @Date 2021/8/17
 */
@NoRepositoryBean
public interface CrudRepository<T, ID> extends Repository<T, ID> {

    /**
     * <h2> 保存一个给定的实体到该索引下 </h2>
     *
     * @param entity  must not be {@literal null}.
     * @param routing 指定路由 (也可以在entity 中 添加一个property {@link Routing}
     * @return 返回 entity
     */
    <S extends T> S save(S entity, String routing);

    /**
     * <h2> 保存给定的所有实体 </h2>
     *
     * @param entities 所有实体
     *                 routing 指定路由 (也可以在entity 中 添加一个property {@link Routing}
     * @return 返回 entities
     */
    <S extends T> Iterable<S> saveAll(Iterable<S> entities, String routing);

    /**
     * <h2>  通过其 id 检索实体 </h2>
     *
     * @param id      must not be {@literal null}.
     * @param routing 指定路由
     * @return 返回匹配到的实体
     */
    Optional<T> findById(ID id, String routing) throws Exception;

    /**
     * <h2> 返回所有实体 </h2>
     *
     * @param routing 指定路由
     */
    Iterable<T> findAll(String routing);

    Page<T> findAll(Pageable pageable, String routing);

    /**
     * <h2> 返回给定Id 匹配到的实体 </h2>
     *
     * @param ids     must not be {@literal null}.
     * @param routing 指定路由
     */
    Iterable<T> findAllById(Iterable<ID> ids, String routing);

    /**
     * <h2> 返回给定实体的数量 </h2>
     *
     * @param routing 指定路由
     * @return the number of entities.
     */
    long count(String routing);

    /**
     * <h2> 根据Id删除一个给定的实体 </h2>
     *
     * @param routing 指定路由
     * @param id      must not be {@literal null}.
     */
    void deleteById(ID id, String routing);

    /**
     * <h2> 删除一个给定实体 </h2>
     *
     * @param entity  must not be {@literal null}.
     * @param routing 指定路由 (也可以在entity 中 添加一个property {@link Routing}
     */
    void delete(T entity, String routing);

    /**
     * <h2> 删除所有实体 </h2>
     *
     * @param entities 所有实体
     * @param routing  指定路由 (也可以在entity 中 添加一个property {@link Routing}
     */
    void deleteAll(Iterable<? extends T> entities, String routing);

    /**
     * <h2> 删除当前路由下的实体 </h2>
     *
     * @param routing 指定路由
     */
    void deleteAll(String routing);

}
