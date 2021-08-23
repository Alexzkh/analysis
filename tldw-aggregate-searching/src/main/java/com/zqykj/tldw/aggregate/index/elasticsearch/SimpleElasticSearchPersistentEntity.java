/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.index.elasticsearch;

import com.zqykj.annotations.Setting;
import com.zqykj.infrastructure.util.TypeInformation;
import com.zqykj.tldw.aggregate.index.mapping.BasicPersistentEntity;
import org.elasticsearch.common.collect.MapBuilder;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.index.VersionType;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.TimeUnit;

import com.zqykj.annotations.Document;

/**
 * <h1> Elasticsearch persistent entity information describe,the notes {@link Document}</h1>
 */
public class SimpleElasticSearchPersistentEntity<T>
        extends BasicPersistentEntity<T, SimpleElasticSearchPersistentProperty> implements ElasticsearchPersistentEntity<T> {

    @Nullable
    private String indexName;
    private boolean useServerConfiguration;
    private short shards;
    private short replicas;
    @Nullable
    private String refreshInterval;
    @Nullable
    private String indexStoreType;
    @Nullable
    private VersionType versionType;
    private boolean createIndexAndMapping;
    @Nullable
    private String settingPath;
    private final Map<String, SimpleElasticSearchPersistentProperty> fieldNamePropertyCache = new ConcurrentHashMap<>();
    private boolean rollover;
    private boolean autoRollover;
    private long autoRolloverInitialDelay;
    private long autoRolloverPeriod;
    private TimeUnit autoRolloverTimeUnit;
    private long rolloverMaxIndexAgeCondition;
    private TimeUnit rolloverMaxIndexAgeTimeUnit;
    private long rolloverMaxIndexDocsCondition;
    private long rolloverMaxIndexSizeCondition;
    private ByteSizeUnit rolloverMaxIndexSizeByteSizeUnit;
    private SimpleElasticSearchPersistentProperty routingFieldProperty;

    public SimpleElasticSearchPersistentEntity(TypeInformation<T> typeInformation) {

        super(typeInformation);
        Class<T> clazz = typeInformation.getType();
        if (clazz.isAnnotationPresent(Document.class)) {
            Document document = clazz.getAnnotation(Document.class);
            Assert.hasText(document.indexName(),
                    " Unknown indexName. Make sure the indexName is defined. e.g @Document(indexName=\"foo\")");
            this.indexName = document.indexName();
            this.useServerConfiguration = document.useServerConfiguration();
            this.shards = document.shards();
            this.replicas = document.replicas();
            this.refreshInterval = document.refreshInterval();
            this.indexStoreType = document.indexStoreType();
            this.versionType = document.versionType();
            this.createIndexAndMapping = document.createIndex();
            if (clazz.isAnnotationPresent(Setting.class)) {
                this.settingPath = typeInformation.getType().getAnnotation(Setting.class).settingPath();
            }
            this.rollover = document.rollover();
            this.autoRollover = document.autoRollover();
            this.autoRolloverInitialDelay = document.autoRolloverInitialDelay();
            this.autoRolloverPeriod = document.autoRolloverPeriod();
            this.autoRolloverTimeUnit = document.autoRolloverTimeUnit();
            this.rolloverMaxIndexAgeCondition = document.rolloverMaxIndexAgeCondition();
            this.rolloverMaxIndexAgeTimeUnit = document.rolloverMaxIndexAgeTimeUnit();
            this.rolloverMaxIndexDocsCondition = document.rolloverMaxIndexDocsCondition();
            this.rolloverMaxIndexSizeCondition = document.rolloverMaxIndexSizeCondition();
            this.rolloverMaxIndexSizeByteSizeUnit = document.rolloverMaxIndexSizeByteSizeUnit();
        }
    }


    @Nullable
    @Override
    public String getIndexStoreType() {
        return indexStoreType;
    }

    @Nullable
    @Override
    public String getIndexName() {
        return indexName;
    }

    @Override
    public short getShards() {
        return shards;
    }

    @Override
    public short getReplicas() {
        return replicas;
    }

    @Override
    public boolean isUseServerConfiguration() {
        return useServerConfiguration;
    }

    @Nullable
    @Override
    public String getRefreshInterval() {
        return refreshInterval;
    }

    @Nullable
    @Override
    public VersionType getVersionType() {
        return versionType;
    }

    @Override
    public String settingPath() {
        return settingPath;
    }

    @Override
    public boolean isCreateIndexAndMapping() {
        return createIndexAndMapping;
    }

    public void setIndexName(@Nullable String indexName) {
        this.indexName = indexName;
    }

    public boolean isRollover() {
        return rollover;
    }

    public void setRollover(boolean rollover) {
        this.rollover = rollover;
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

    @Nullable
    @Override
    public SimpleElasticSearchPersistentProperty getPersistentPropertyWithFieldName(String fieldName) {

        Assert.notNull(fieldName, "fieldName must not be null");

        return fieldNamePropertyCache.computeIfAbsent(fieldName, key -> {
            AtomicReference<SimpleElasticSearchPersistentProperty> propertyRef = new AtomicReference<>();
            doWithProperties(property -> {
                if (key.equals(property.getFieldName())) {
                    propertyRef.set(property);
                }
            });

            return propertyRef.get();
        });
    }

    /**
     * <h2> 获取默认的settings 参数 </h2>
     */
    public Map<String, ?> getDefaultSettings() {
        if (isUseServerConfiguration()) {
            return new LinkedHashMap<>();
        }
        return new MapBuilder<String, Object>()
                .put("index.number_of_shards", String.valueOf(getShards()))
                .put("index.number_of_replicas", String.valueOf(getReplicas()))
                .put("index.refresh_interval", getRefreshInterval()).put("index.store.type", getIndexStoreType()).map();
    }
}
