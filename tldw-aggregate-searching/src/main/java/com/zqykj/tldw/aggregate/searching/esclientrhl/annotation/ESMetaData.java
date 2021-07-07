package com.zqykj.tldw.aggregate.searching.esclientrhl.annotation;


import org.elasticsearch.common.unit.ByteSizeUnit;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * The annotation of elasticsearch metadata ,is added to eleasticsearch entity class .
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface ESMetaData {
    /**
     * If not configured, the index name is the same as indexname by default. This annotation only supports search.
     * It is not recommended to do this. It is recommended to do cross index queries through specific methods.
     */
    String[] searchIndexNames() default {};

    /**
     * Index name, must be configured.
     */
    String indexName();

    /**
     * The index type can not be configured. It is the same as indexname by default.
     * It is recommended that there is only one type under each index
     */
    String indexType() default "";

    /**
     * shard number
     */
    int number_of_shards() default 1;

    /**
     * replicas number.
     */
    int number_of_replicas() default 1;

    /**
     * Print log or not
     */
    boolean printLog() default false;

    /**
     * Aliases, if configured, are based on this alias
     * When this item is configured, the automatic index creation function will be disabled
     * Indexname is AliasName
     */
    boolean alias() default false;

    /**
     * The index name corresponding to the alias
     * The current configuration only takes effect when alias is configured but rollover is not
     * Note: All configured indexes must exist
     */
    String[] aliasIndex() default {};

    /**
     * After configuring alias, specify which index is writeindex
     * The current configuration only takes effect when alias is configured but rollover is not
     * Note: the configured index must exist in the alisindex
     */
    String writeIndex() default "";

    /**
     * When the rollover is configured to true, turn on the rollover function (and ignore the configuration of other aliases)
     * AliasName is indexname
     * Index name specification: indexname-yyyy.mm.dd-00000n
     * The index rolling generation strategy is as follows
     */
    boolean rollover() default false;


    /**
     * Automatic rollover configuration
     * Automatic roll over switch
     */
    boolean autoRollover() default false;

    /**
     * Automatic rollover configuration
     * The project starts after the autorollover initial delay time is delayed
     */
    long autoRolloverInitialDelay() default 0L;

    /**
     * Automatic rollover configuration
     * Once every autorollover period after the project starts
     */
    long autoRolloverPeriod() default 4L;

    /**
     * Automatic rollover configuration
     * Unit time configuration, corresponding to autorollover period and autorollover initial delay
     */
    TimeUnit autoRolloverTimeUnit() default TimeUnit.HOURS;

    /**
     * A new index is generated after the current index exceeds the configured time
     */
    long rolloverMaxIndexAgeCondition() default 0L;

    /**
     * In combination with rollover maxindexagecondition, it corresponds to the unit of rollover maxindexagecondition
     */
    TimeUnit rolloverMaxIndexAgeTimeUnit() default TimeUnit.DAYS;

    /**
     * A new index will be generated when the number of current index documents exceeds the number configured for this item
     */
    long rolloverMaxIndexDocsCondition() default 0L;

    /**
     * A new index is generated when the current index size exceeds the number configured for this item
     */
    long rolloverMaxIndexSizeCondition() default 0L;

    /**
     * In combination with rollover maxindexsize condition, it corresponds to the unit of rollover maxindexsize condition
     */
    ByteSizeUnit rolloverMaxIndexSizeByteSizeUnit() default ByteSizeUnit.GB;

    /**
     * the max pageable result .
     */
    long maxResultWindow() default 10000L;

    /**
     * is auto index name include suffix .
     */
    boolean suffix() default false;

    /**
     * is auto create index
     */
    boolean autoCreateIndex() default true;

}
