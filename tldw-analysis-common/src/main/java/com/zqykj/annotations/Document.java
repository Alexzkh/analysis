package com.zqykj.annotations;

import org.elasticsearch.index.VersionType;

import java.lang.annotation.*;

/**
 * <h1> 标识要持久化到Es 、 Mongodb的域对象 </h1>
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Document {

    String indexName();

    short shards() default 1;

    short replicas() default 0;

    boolean createIndex() default true;

    /**
     * Use server-side settings when creating the index.
     */
    boolean useServerConfiguration() default false;

    /**
     * Refresh interval for the index {@link #indexName()}. Used for index creation.
     */
    String refreshInterval() default "2s";

    /**
     * Index storage type for the index {@link #indexName()}. Used for index creation.
     */
    String indexStoreType() default "fs";

    /**
     * Configuration of version management.
     */
    VersionType versionType() default VersionType.EXTERNAL;

    /**
     * Defines the default language to be used with this document.
     *
     * @return an empty String by default.
     * @since 1.6
     */
    String language() default "";

}
