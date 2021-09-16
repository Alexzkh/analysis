package com.zqykj.repository;

import com.zqykj.annotations.NoRepositoryBean;
import com.zqykj.domain.Page;
import com.zqykj.domain.Pageable;
import com.zqykj.domain.Routing;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Optional;

/**
 * Interface for generic CRUD operations on a operations for a specific type.
 *
 * <T>  the domain type the operations manages
 * <ID> the type of the id of the entity the operations manages
 *
 * @Author zhangkehou
 * @Date 2021/8/17
 */
@NoRepositoryBean
public interface CrudRepository extends Repository {

    /**
     * <h2> 保存 / 更新(需要指定Id) 当前索引下 给定的一个文档数据 </h2>
     *
     * @param entity      must not be {@literal null}.
     * @param routing     指定路由 (也可以在entity 中 添加一个property {@link Routing}
     * @param entityClass 索引实体类
     */
    <T> T save(T entity, @Nullable String routing, @NonNull Class<T> entityClass);

    /**
     * <h2> 保存 / 更新(需要指定Id) 当前索引下 给定的一组文档数据 </h2>
     *
     * @param entities    一组文档数据
     * @param routing     指定路由 (也可以在entity 中 添加一个property {@link Routing}
     * @param entityClass 索引实体类
     */
    <T> Iterable<T> saveAll(Iterable<T> entities, @Nullable String routing, @NonNull Class<T> entityClass);

    /**
     * <h2> 根据文档Id 、路由(可选) 查询 当前索引下的一个文档 </h2>
     *
     * @param id          文档Id    must not be {@literal null}.
     * @param routing     指定路由
     * @param entityClass 索引实体类
     */
    <T, ID> Optional<T> findById(ID id, @Nullable String routing, @NonNull Class<T> entityClass) throws Exception;

    /**
     * <h2> 根据路由(可选)  查询当前索引下的文档数据(默认分页) </h2>
     *
     * @param routing     指定路由
     * @param entityClass 索引实体类
     */
    <T> Iterable<T> findAll(@Nullable String routing, @NonNull Class<T> entityClass);

    /**
     * <h2> 根据路由(可选)  分页查询当前索引下的文档数据 </h2>
     *
     * @param pageable    分页参数  must not be {@literal null}.
     * @param routing     指定路由
     * @param entityClass 索引实体类 must not be {@literal null}.
     */
    <T> Page<T> findAll(Pageable pageable, @Nullable String routing, @NonNull Class<T> entityClass);

    /**
     * <h2> 根据文档Id 、路由 查询当前索引下指定的文档  </h2>
     *
     * @param ids         文档Id集合 must not be {@literal null}.
     * @param routing     指定路由
     * @param entityClass 索引实体类 must not be {@literal null}.
     */
    <T, ID> Iterable<T> findAllById(Iterable<ID> ids, @Nullable String routing, @NonNull Class<T> entityClass);

    /**
     * <h2> 根据 路由(可选) 查询当前索引下的全部文档数量 </h2>
     *
     * @param routing     指定路由
     * @param entityClass 索引实体类  must not be {@literal null}.
     * @return the number of entities.
     */
    <T> long count(@Nullable String routing, @NonNull Class<T> entityClass);

    /**
     * <h2> 根据Id、路由 删除当前索引下的一个文档 </h2>
     *
     * @param id          文档Id  must not be {@literal null}.
     * @param routing     指定路由
     * @param entityClass 索引实体类 must not be {@literal null}.
     */
    <T, ID> void deleteById(ID id, @Nullable String routing, @NonNull Class<T> entityClass);

    /**
     * <h2> 根据文档Id 、路由 删除当前索引下的文档  </h2>
     *
     * @param ids         文档Id    must not be {@literal null}.
     * @param routing     指定路由
     * @param entityClass 索引实体类  must not be {@literal null}.
     */
    <T, ID> void deleteAll(Iterable<ID> ids, @Nullable String routing, @NonNull Class<T> entityClass);

    /**
     * <h2> 根据路由删除当前索引下的文档 </h2>
     *
     * @param routing     指定路由
     * @param entityClass 索引实体类  must not be {@literal null}.
     */
    <T> void deleteAll(@Nullable String routing, @NonNull Class<T> entityClass);
}
