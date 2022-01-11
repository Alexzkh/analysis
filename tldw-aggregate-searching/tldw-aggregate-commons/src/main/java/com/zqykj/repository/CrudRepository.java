package com.zqykj.repository;

import com.zqykj.annotations.NoRepositoryBean;
import com.zqykj.common.request.QueryParams;
import com.zqykj.domain.Page;
import com.zqykj.domain.Pageable;
import com.zqykj.domain.Routing;
import com.zqykj.parameters.query.QuerySpecialParams;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;
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
     * <h2> 保存 / 更新(需要指定Id) 给定的数据 </h2>
     *
     * @param entity      实体数据 must not be {@literal null}.
     * @param routing     路由参数 (也可以在entity 中 添加一个property {@link Routing) 指明路由
     * @param entityClass 实体类   must not be {@literal null}.
     */
    <T> T save(T entity, @Nullable String routing, @NonNull Class<T> entityClass);

    /**
     * <h2> 保存 / 更新(需要指定Id) 给定的一组数据集合 </h2>
     *
     * @param entities    一组实体数据集合   must not be {@literal null}.
     * @param routing     路由参数 (也可以在entities 的一个实体 中 添加一个property {@link Routing) 指明路由
     * @param entityClass 实体类            must not be {@literal null}.
     */
    <T> Iterable<T> saveAll(Iterable<T> entities, @Nullable String routing, @NonNull Class<T> entityClass);

    /**
     * <h2> 保存/更新(需要指定Id) 给定一组数据集合</h2>
     *
     * @param values      一组map的数据集合
     * @param entityClass 索引实体
     */
    <T> void saveAll(List<Map<String, ?>> values, @Nullable String routing, @NonNull Class<T> entityClass);

    /**
     * <h2> 根据 Id检索数据 </h2>
     * <p>
     * 同一个路由下 id 数据不会重复(eg. 因此对应案件来说不会出现数据错乱问题)
     *
     * @param id          数据id    must not be {@literal null}.
     * @param routing     路由参数
     * @param entityClass 实体类    must not be {@literal null}.
     */
    <T, ID> Optional<T> findById(ID id, @Nullable String routing, @NonNull Class<T> entityClass) throws Exception;

    /**
     * <h2> 检索所有数据 </h2>
     * <p>
     * 注意: 最多返回100MB的数据量(对于es来说) 100*1024*1024(堆buffer) 如果需要修改 eg. es 可以再RequestOptions中修改
     *
     * @param routing     路由参数
     * @param entityClass 实体类  must not be {@literal null}.
     */
    <T> Iterable<T> findAll(@Nullable String routing, @NonNull Class<T> entityClass);

    /**
     * <h2> 检索所有数据(基于条件查询过滤) </h2>
     * <p>
     * 注意: 最多返回100MB的数据量(对于es来说) 100*1024*1024(堆buffer) 如果需要修改 eg. es 可以再RequestOptions中修改
     *
     * @param routing     路由参数
     * @param entityClass 实体类  must not be {@literal null}.
     * @param condition   条件查询
     */
    <T> Iterable<T> findAll(@NonNull Class<T> entityClass, @Nullable String routing, @Nullable QuerySpecialParams condition);

    /**
     * <h2> 根据路由(可选)  分页查询当前索引下的文档数据 </h2>
     * <p>
     * 注意: eg. es的话默认是分页是有限制的, 默认from + size 需要 <= 10000, 如果需要修改, 可以对es的集群进行重新设置
     * <p>
     * 目前设置的是 from + size <= 100_0000
     *
     * @param pageable    分页参数  must not be {@literal null}.
     * @param routing     路由参数
     * @param entityClass 实体类    must not be {@literal null}.
     */
    <T> Page<T> findAll(Pageable pageable, @Nullable String routing, @NonNull Class<T> entityClass);

    /**
     * <h2> 分页查询(基于条件查询过滤) </h2>
     *
     * @param pageable    分页参数  must not be {@literal null}.
     * @param routing     路由参数
     * @param entityClass 实体类    must not be {@literal null}.
     * @param condition   条件查询
     */
    <T> Page<T> findAll(Pageable pageable, @Nullable String routing, @NonNull Class<T> entityClass, @Nullable QuerySpecialParams condition);


    /**
     * <h2> 根据一组id数据集合检索数据  </h2>
     * <p>
     * 同一个路由下 id 数据不会重复(eg. 因此对应案件来说不会出现数据错乱问题)
     *
     * @param ids         一组Id集合 must not be {@literal null}.
     * @param routing     路由参数
     * @param entityClass 实体类     must not be {@literal null}.
     */
    <T, ID> Iterable<T> findAllById(Iterable<ID> ids, @Nullable String routing, @NonNull Class<T> entityClass);

    /**
     * <h2> 删除数据 </h2>
     *
     * @param queryParams 查询参数
     * @param entityClass 实体类   must not be {@literal null}.
     */
    <T> T query(QueryParams queryParams, Class<T> entityClass);

    /**
     * <h2> 统计数量 </h2>
     *
     * @param routing     路由参数
     * @param entityClass 实体类     must not be {@literal null}.
     * @return the number of entities.
     */
    <T> long count(@Nullable String routing, @NonNull Class<T> entityClass);

    /**
     * <h2> 统计总数据量(基于条件查询过滤) </h2>
     *
     * @param routing     路由参数
     * @param entityClass 实体类     must not be {@literal null}.
     * @param condition   条件查询
     */
    <T> long count(@Nullable String routing, @NonNull Class<T> entityClass, @Nullable QuerySpecialParams condition);

    /**
     * <h2> 根据Id 删除数据 </h2>
     * <p>
     * 同一个路由下 id 数据不会重复(eg. 因此对应案件来说不会出现数据错乱问题)
     *
     * @param id          数据id       must not be {@literal null}.
     * @param routing     路由参数
     * @param entityClass 实体类       must not be {@literal null}.
     */
    <T, ID> void deleteById(ID id, @Nullable String routing, @NonNull Class<T> entityClass);

    /**
     * <h2> 根据一组Id 集合 删除数据  </h2>
     * <p>
     * 同一个路由下 id 数据不会重复(eg. 因此对应案件来说不会出现数据错乱问题)
     *
     * @param ids         数据id    must not be {@literal null}.
     * @param routing     路由参数
     * @param entityClass 实体类    must not be {@literal null}.
     */
    <T, ID> void deleteAll(Iterable<ID> ids, @Nullable String routing, @NonNull Class<T> entityClass);

    /**
     * <h2> 根据条件删除 </h2>
     *
     * @param routing     路由
     * @param entityClass 索引类
     * @param condition   删除条件
     */
    <T> void delete(@Nullable String routing, @NonNull Class<T> entityClass, @Nullable QuerySpecialParams condition);

    /**
     * <h2> 删除数据 </h2>
     * <p>
     *
     * @param routing     指定路由
     * @param entityClass 实体类   must not be {@literal null}.
     */
    <T> void deleteAll(@Nullable String routing, @NonNull Class<T> entityClass);

    /**
     * <h2> 删除数据(基于条件查询过滤) </h2>
     * <p>
     *
     * @param routing     指定路由
     * @param entityClass 实体类   must not be {@literal null}.
     * @param condition   条件查询
     */
    <T> void deleteAll(@Nullable String routing, @NonNull Class<T> entityClass, QuerySpecialParams condition);

    /**
     * <h2> 根据路由(可选)  分页查询当前索引下的文档数据 </h2>
     *
     * @param pageable    分页参数  must not be {@literal null}.
     * @param routing     路由参数
     * @param entityClass 实体类    must not be {@literal null}.
     */
    <T> Page<T> findByCondition(Pageable pageable, @Nullable String routing, @NonNull Class<T> entityClass, String... values);
}
