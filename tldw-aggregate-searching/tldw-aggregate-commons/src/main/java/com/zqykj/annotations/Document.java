package com.zqykj.annotations;


import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.index.VersionType;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * <h1> 标识要持久化到Es 、 Mongodb的域对象 </h1>
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Document {

    String indexName();

    /**
     * the numbers of shards defult 1.
     */
    short shards() default 1;

    /**
     * the numbers of replicas defult 0.
     */
    short replicas() default 0;

    /**
     * is auto create index.
     */
    boolean createIndex() default true;

    /**
     * Use server-side settings when creating the index.
     */
    boolean useServerConfiguration() default false;

    /**
     * Refresh interval for the index {@link #indexName()}. Used for index creation.
     */
    String refreshInterval() default "60s";

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
     */
    String language() default "";

    /**
     * When the configured rollover is true, turn on the rollover function (and ignore the configuration of other alias).
     * An example of an index name is：indexName-yyyy.mm.dd-00000n .
     */
    boolean rollover() default false;


    /**
     * Auto roll over switch
     */
    boolean autoRollover() default false;

    /**
     * After the project is started, the execution starts after delaying the autorolloverinitialdelay time .
     */
    long autoRolloverInitialDelay() default 0L;

    /**
     * After the project is started , the execution start after autoRolloverPeriod time .
     */
    long autoRolloverPeriod() default 4L;

    /**
     * Unit time configuration，autoRolloverPeriod、autoRolloverInitialDelay are relevant.
     */
    TimeUnit autoRolloverTimeUnit() default TimeUnit.HOURS;

    /**
     * Generate a new index after the current index exceeds the time configured for this item.
     */
    long rolloverMaxIndexAgeCondition() default 0L;

    /**
     * Used in combination with rollovermaxindexagecondition, it corresponds to the unit of rollovermaxindexagecondition.
     */
    TimeUnit rolloverMaxIndexAgeTimeUnit() default TimeUnit.DAYS;

    /**
     * A new index will be generated when the number of currently indexed documents exceeds the number configured for this item.
     */
    long rolloverMaxIndexDocsCondition() default 0L;

    /**
     * Generate a new index when the current index size exceeds the number configured for this item.
     */
    long rolloverMaxIndexSizeCondition() default 0L;

    /**
     * Used in combination with rollovermaxindexsizecondition, the unit corresponding to rollovermaxindexsizecondition.
     */
    ByteSizeUnit rolloverMaxIndexSizeByteSizeUnit() default ByteSizeUnit.GB;
}
