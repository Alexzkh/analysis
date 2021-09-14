/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zqykj.annotations;

import java.lang.annotation.*;

/**
 * The {@link Sharded} annotation provides meta information about the actual distribution of data. The
 * {@link #shardKey()} is used to distribute documents across shards. <br />
 * Please see the <a href="https://docs.mongodb.com/manual/sharding/">MongoDB Documentation</a> for more information
 * about requirements and limitations of sharding.
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface Sharded {


    String[] shardKey() default {};

    /**
     * The sharding strategy to use for distributing data across sharded clusters.
     *
     * @return {@link ShardingStrategy#RANGE} by default
     */
    ShardingStrategy shardingStrategy() default ShardingStrategy.RANGE;

    /**
     * As of MongoDB 4.2 it is possible to change the shard key using update. Using immutable shard keys avoids server
     * round trips to obtain an entities actual shard key from the database.
     *
     * @return {@literal false} by default.
     * @see <a href="https://docs.mongodb.com/manual/core/sharding-shard-key/#change-a-document-s-shard-key-value">MongoDB
     * Reference: Change a Document's Shard Key Value</a>
     */
    boolean immutableKey() default false;

    enum ShardingStrategy {

        /**
         * Ranged sharding involves dividing data into ranges based on the shard key values. Each chunk is then assigned a
         * range based on the shard key values.
         */
        RANGE,

        /**
         * Hashed Sharding involves computing a hash of the shard key field’s value. Each chunk is then assigned a range based
         * on the hashed shard key values.
         */
        HASH
    }
}
