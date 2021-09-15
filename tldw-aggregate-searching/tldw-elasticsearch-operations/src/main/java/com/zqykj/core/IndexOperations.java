/**
 * @作者 Mcj
 */
package com.zqykj.core;


import com.zqykj.core.document.Document;

/**
 * <h1> Elasticsearch 索引操作 </h1>
 */
public interface IndexOperations {

    /**
     * <h2> 创建索引 </h2>
     */
    boolean create();

    /**
     * <h2> 创建索引分片 </h2>
     */
    boolean createIndexRollover();

    boolean createOrRollover();

    /**
     * <h2> 通过给定的配置创建索引 </h2>
     *
     * @param settings the index settings
     * @return {@literal true} if the index was created
     */
    boolean create(Document settings);

    /**
     * <h2> 通过保绑定的索引类 删除索引 </h2>
     *
     * @return {@literal true} if the index was deleted
     */
    boolean delete();

    /**
     * <h2> 检查此IndexOperations实例上 绑定的索引是否存在 </h2>
     *
     * @return {@literal true} if the index exists
     */
    boolean exists();

    /**
     * <h2> 刷新此IndexOperations实例上 绑定的索引 </h2>
     */
    void refresh();

    /**
     * <h2> 为该 IndexOperations实例上绑定的索引创建 索引映射 </h2>
     *
     * @return mapping object
     */
    Document createMapping();

    /**
     * <h2> 创建给定类的映射 </h2>
     *
     * @param clazz the clazz to create a mapping for
     * @return mapping object
     */
    Document createMapping(Class<?> clazz);

    /**
     * <h2> 将映射写入此 IndexOperations实例 绑定到的类的索引 </h2>
     *
     * @return {@literal true} if the mapping could be stored
     * @since 4.1
     */
    default boolean putMapping() {
        return putMapping(createMapping());
    }

    /**
     * <h2> 将映射写入索引 </h2>
     *
     * @param mapping the Document with the mapping definitions
     * @return {@literal true} if the mapping could be stored
     */
    boolean putMapping(Document mapping);

    default boolean putMapping(Class<?> clazz) {
        return putMapping(createMapping(clazz));
    }

    /**
     * <h2> 为该 IndexOperations 绑定到的实体创建索引设置 </h2>
     *
     * @return a settings document.
     * @since 4.1
     */
    Document createSettings();

    /**
     * <h2> 根据给定类的注释创建索引设置 </h2>
     *
     * @param clazz the class to create the index settings from
     * @return a settings document.
     * @since 4.1
     */
    Document createSettings(Class<?> clazz);

    /**
     * <h2> 获取索引名称 </h2>
     */
    String getIndexCoordinates();

    String getRolloverIndexCoordinates();

    void rollover(boolean isAsync) throws Exception;
}
