package com.zqykj.tldw.aggregate.searching.esclientrhl.util;

import org.elasticsearch.common.unit.ByteSizeUnit;

import java.util.concurrent.TimeUnit;

/**
 * Metadata carrier class
 */
public class MetaData {

    /**
     * Index name, must be configured
     */
    private String indexname = "";

    /**
     * The index type can not be configured. It is the same as indexname by default.
     * It is recommended that there is only one type under each index
     */
    private String indextype = "";

    /**
     * If not configured, the index name is the same as indexname by default. This annotation only supports search.
     * It is not recommended to do this. It is recommended to do cross index queries through specific methods.
     */
    private String[] searchIndexNames;

    /**
     * shard number
     */
    private int number_of_shards;

    /**
     * replicas number.
     */
    private int number_of_replicas;


    /**
     * replicas number.
     */
    private boolean printLog = false;

    /**
     * Aliases, if configured, are based on this alias
     * When this item is configured, the automatic index creation function will be disabled
     * Indexname is AliasName
     */
    private boolean alias;

    /**
     * The index name corresponding to the alias
     * The current configuration only takes effect when alias is configured but rollover is not
     * Note: All configured indexes must exist
     */
    private String[] aliasIndex;

    /**
     * After configuring alias, specify which index is writeindex
     * The current configuration only takes effect when alias is configured but rollover is not
     * Note: the configured index must exist in the alisindex
     */
    private String writeIndex;

    /**
     * When the rollover is configured to true, turn on the rollover function (and ignore the configuration of other aliases)
     * AliasName is indexname
     * Index name specification: indexname-yyyy.mm.dd-00000n
     * The index rolling generation strategy is as follows
     */
    private boolean rollover;

    /**
     * A new index is generated after the current index exceeds the configured time
     */
    private long rolloverMaxIndexAgeCondition;

    /**
     * In combination with rollover maxindexagecondition, it corresponds to the unit of rollover maxindexagecondition
     */
    private TimeUnit rolloverMaxIndexAgeTimeUnit;

    /**
     * A new index will be generated when the number of current index documents exceeds the number configured for this item
     */
    private long rolloverMaxIndexDocsCondition;

    /**
     * A new index is generated when the current index size exceeds the number configured for this item
     */
    private long rolloverMaxIndexSizeCondition;

    /**
     * In combination with rollover maxindexsize condition, it corresponds to the unit of rollover maxindexsize condition
     */
    private ByteSizeUnit rolloverMaxIndexSizeByteSizeUnit;

    /**
     * Automatic rollover configuration
     * Automatic roll over switch
     */
    private boolean autoRollover;

    /**
     * Automatic rollover configuration
     * The project starts after the autorollover initial delay time is delayed
     */
    private long autoRolloverInitialDelay;

    /**
     * Automatic rollover configuration
     * Once every autorollover period after the project starts
     */
    private long autoRolloverPeriod;

    /**
     * Automatic rollover configuration
     * Unit time configuration, corresponding to autorollover period and autorollover initial delay
     */
    private TimeUnit autoRolloverTimeUnit;

    /**
     * is auto index name include suffix .The suffix of indexname is generally used to distinguish the environment in configuration.
     */
    private String suffix;

    /**
     * is auto create index
     */
    private boolean autoCreateIndex;

    /**
     * the max pageable result .
     */
    private long maxResultWindow;

    public MetaData(String indexname, String indextype) {
        this.indexname = indexname;
        this.indextype = indextype;
    }

    public MetaData(String indexname, String indextype, int number_of_shards, int number_of_replicas) {
        this.indexname = indexname;
        this.indextype = indextype;
        this.number_of_shards = number_of_shards;
        this.number_of_replicas = number_of_replicas;
    }

    public MetaData(int number_of_shards, int number_of_replicas) {
        this.number_of_shards = number_of_shards;
        this.number_of_replicas = number_of_replicas;
    }

    public String[] getSearchIndexNames() {
        return searchIndexNames;
    }

    public void setSearchIndexNames(String[] searchIndexNames) {
        this.searchIndexNames = searchIndexNames;
    }

    public boolean isPrintLog() {
        return printLog;
    }

    public void setPrintLog(boolean printLog) {
        this.printLog = printLog;
    }

    public String getIndexname() {
        return indexname;
    }

    public void setIndexname(String indexname) {
        this.indexname = indexname;
    }

    public String getIndextype() {
        return indextype;
    }

    public void setIndextype(String indextype) {
        this.indextype = indextype;
    }

    public int getNumber_of_shards() {
        return number_of_shards;
    }

    public void setNumber_of_shards(int number_of_shards) {
        this.number_of_shards = number_of_shards;
    }

    public int getNumber_of_replicas() {
        return number_of_replicas;
    }

    public void setNumber_of_replicas(int number_of_replicas) {
        this.number_of_replicas = number_of_replicas;
    }

    public long getMaxResultWindow() {
        return maxResultWindow;
    }

    public void setMaxResultWindow(long maxResultWindow) {
        this.maxResultWindow = maxResultWindow;
    }

    public boolean isAlias() {
        return alias;
    }

    public void setAlias(boolean alias) {
        this.alias = alias;
    }

    public String[] getAliasIndex() {
        return aliasIndex;
    }

    public void setAliasIndex(String[] aliasIndex) {
        this.aliasIndex = aliasIndex;
    }

    public String getWriteIndex() {
        return writeIndex;
    }

    public void setWriteIndex(String writeIndex) {
        this.writeIndex = writeIndex;
    }

    public boolean isRollover() {
        return rollover;
    }

    public void setRollover(boolean rollover) {
        this.rollover = rollover;
    }

    public long getRolloverMaxIndexAgeCondition() {
        return rolloverMaxIndexAgeCondition;
    }

    public void setRolloverMaxIndexAgeCondition(long rolloverMaxIndexAgeCondition) {
        this.rolloverMaxIndexAgeCondition = rolloverMaxIndexAgeCondition;
    }

    public TimeUnit getRolloverMaxIndexAgeTimeUnit() {
        return rolloverMaxIndexAgeTimeUnit;
    }

    public void setRolloverMaxIndexAgeTimeUnit(TimeUnit rolloverMaxIndexAgeTimeUnit) {
        this.rolloverMaxIndexAgeTimeUnit = rolloverMaxIndexAgeTimeUnit;
    }

    public long getRolloverMaxIndexDocsCondition() {
        return rolloverMaxIndexDocsCondition;
    }

    public void setRolloverMaxIndexDocsCondition(long rolloverMaxIndexDocsCondition) {
        this.rolloverMaxIndexDocsCondition = rolloverMaxIndexDocsCondition;
    }

    public long getRolloverMaxIndexSizeCondition() {
        return rolloverMaxIndexSizeCondition;
    }

    public void setRolloverMaxIndexSizeCondition(long rolloverMaxIndexSizeCondition) {
        this.rolloverMaxIndexSizeCondition = rolloverMaxIndexSizeCondition;
    }

    public ByteSizeUnit getRolloverMaxIndexSizeByteSizeUnit() {
        return rolloverMaxIndexSizeByteSizeUnit;
    }

    public void setRolloverMaxIndexSizeByteSizeUnit(ByteSizeUnit rolloverMaxIndexSizeByteSizeUnit) {
        this.rolloverMaxIndexSizeByteSizeUnit = rolloverMaxIndexSizeByteSizeUnit;
    }

    public boolean isAutoRollover() {
        return autoRollover;
    }

    public void setAutoRollover(boolean autoRollover) {
        this.autoRollover = autoRollover;
    }

    public long getAutoRolloverInitialDelay() {
        return autoRolloverInitialDelay;
    }

    public void setAutoRolloverInitialDelay(long autoRolloverInitialDelay) {
        this.autoRolloverInitialDelay = autoRolloverInitialDelay;
    }

    public long getAutoRolloverPeriod() {
        return autoRolloverPeriod;
    }

    public void setAutoRolloverPeriod(long autoRolloverPeriod) {
        this.autoRolloverPeriod = autoRolloverPeriod;
    }

    public TimeUnit getAutoRolloverTimeUnit() {
        return autoRolloverTimeUnit;
    }

    public void setAutoRolloverTimeUnit(TimeUnit autoRolloverTimeUnit) {
        this.autoRolloverTimeUnit = autoRolloverTimeUnit;
    }


    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public boolean isAutoCreateIndex() {
        return autoCreateIndex;
    }

    public void setAutoCreateIndex(boolean autoCreateIndex) {
        this.autoCreateIndex = autoCreateIndex;
    }
}