/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.index.mongodb;

import com.zqykj.tldw.aggregate.index.mapping.PersistentEntity;
import com.zqykj.tldw.aggregate.index.mongodb.associate.ShardKey;

/**
 * <h1> Mongodb Persistent Entity describe</h1>
 * <p>
 * 这里会定义一些Mongodb persistent entity 特有属性方法获取
 * </p>
 */
public interface MongodbPersistentEntity<T> extends PersistentEntity<T, SimpleMongodbPersistentProperty> {

    /**
     * <h2> 返回集合名称 </h2>
     */
    String getCollection();

    /**
     * <h2> 返回当前entity language </h2>
     */
    String getLanguage();

    /**
     * Get the entities shard key if defined.
     *
     * @return {@link ShardKey#none()} if not not set.
     * @since 3.0
     */
    ShardKey getShardKey();

    /**
     * @return {@literal true} if the {@link #getShardKey() shard key} is sharded.
     * @since 3.0
     */
    default boolean isSharded() {
        return getShardKey().isSharded();
    }
}
