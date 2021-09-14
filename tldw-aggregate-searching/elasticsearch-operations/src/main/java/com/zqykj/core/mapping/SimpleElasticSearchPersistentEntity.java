/**
 * @作者 Mcj
 */
package com.zqykj.core.mapping;

import com.zqykj.annotations.Document;
import com.zqykj.annotations.Setting;
import com.zqykj.domain.Routing;
import com.zqykj.mapping.MappingException;
import com.zqykj.mapping.model.BasicPersistentEntity;
import com.zqykj.util.TypeInformation;
import org.elasticsearch.common.collect.MapBuilder;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.index.VersionType;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * <h1> Elasticsearch persistent entity information describe,the notes {@link Document}</h1>
 */
public class SimpleElasticSearchPersistentEntity<T>
        extends BasicPersistentEntity<T, ElasticsearchPersistentProperty> implements ElasticsearchPersistentEntity<T> {

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
    private final Map<String, ElasticsearchPersistentProperty> fieldNamePropertyCache = new ConcurrentHashMap<>();
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
    private ElasticsearchPersistentProperty routingFieldProperty;

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

    @Override
    public String getIndexName() {
        return indexName != null ? indexName : getTypeInformation().getType().getSimpleName();
    }

    @Nullable
    @Override
    public String getIndexStoreType() {
        return indexStoreType;
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

    @Override
    public void addPersistentProperty(ElasticsearchPersistentProperty property) {
        super.addPersistentProperty(property);

        //TODO 可以判断一些索引类上特有的property 属性 eg.  isSeqNoPrimaryTermProperty、isScoreProperty、isVersionProperty、isParentProperty...

        Class<?> actualType = property.getActualTypeOrNull();

        // 是否包含路由字段
        if (actualType == Routing.class) {
            ElasticsearchPersistentProperty joinProperty = this.routingFieldProperty;

            if (joinProperty != null) {
                throw new MappingException(
                        String.format(
                                "Attempt to add Join property %s but already have property %s registered "
                                        + "as Join property. Check your entity configuration!",
                                property.getField(), joinProperty.getField()));
            }

            this.routingFieldProperty = property;
        }
    }

    /**
     * <h2> 获取给定field name 返回 ElasticsearchPersistentProperty </h2>
     */
    @Nullable
    @Override
    public ElasticsearchPersistentProperty getPersistentPropertyWithFieldName(String fieldName) {

        Assert.notNull(fieldName, "fieldName must not be null");

        return fieldNamePropertyCache.computeIfAbsent(fieldName, key -> {
            AtomicReference<ElasticsearchPersistentProperty> propertyRef = new AtomicReference<>();
            doWithProperties(property -> {
                if (key.equals(property.getFieldName())) {
                    propertyRef.set(property);
                }
            });

            return propertyRef.get();
        });
    }

    public boolean hasRoutingFieldProperty() {
        return routingFieldProperty != null;
    }

    @Nullable
    public ElasticsearchPersistentProperty getRoutingFieldProperty() {
        return routingFieldProperty;
    }

    @Override
    public com.zqykj.core.document.Document getDefaultSettings() {

        if (isUseServerConfiguration()) {
            return com.zqykj.core.document.Document.create();
        }

        Map<String, String> map = new MapBuilder<String, String>()
                .put("index.number_of_shards", String.valueOf(getShards()))
                .put("index.number_of_replicas", String.valueOf(getReplicas()))
                .put("index.refresh_interval", getRefreshInterval()).put("index.store.type", getIndexStoreType()).map();
        return com.zqykj.core.document.Document.from(map);
    }

    @Override
    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    @Override
    public boolean isRollover() {
        return rollover;
    }

    public void setRollover(boolean rollover) {
        this.rollover = rollover;
    }

    @Override
    public boolean isAutoRollover() {
        return autoRollover;
    }

    public void setAutoRollover(boolean autoRollover) {
        this.autoRollover = autoRollover;
    }

    @Override
    public long getAutoRolloverInitialDelay() {
        return autoRolloverInitialDelay;
    }

    public void setAutoRolloverInitialDelay(long autoRolloverInitialDelay) {
        this.autoRolloverInitialDelay = autoRolloverInitialDelay;
    }

    @Override
    public long getAutoRolloverPeriod() {
        return autoRolloverPeriod;
    }

    public void setAutoRolloverPeriod(long autoRolloverPeriod) {
        this.autoRolloverPeriod = autoRolloverPeriod;
    }

    @Override
    public TimeUnit getAutoRolloverTimeUnit() {
        return autoRolloverTimeUnit;
    }

    public void setAutoRolloverTimeUnit(TimeUnit autoRolloverTimeUnit) {
        this.autoRolloverTimeUnit = autoRolloverTimeUnit;
    }

    @Override
    public long getRolloverMaxIndexAgeCondition() {
        return rolloverMaxIndexAgeCondition;
    }

    public void setRolloverMaxIndexAgeCondition(long rolloverMaxIndexAgeCondition) {
        this.rolloverMaxIndexAgeCondition = rolloverMaxIndexAgeCondition;
    }

    @Override
    public TimeUnit getRolloverMaxIndexAgeTimeUnit() {
        return rolloverMaxIndexAgeTimeUnit;
    }

    public void setRolloverMaxIndexAgeTimeUnit(TimeUnit rolloverMaxIndexAgeTimeUnit) {
        this.rolloverMaxIndexAgeTimeUnit = rolloverMaxIndexAgeTimeUnit;
    }

    @Override
    public long getRolloverMaxIndexDocsCondition() {
        return rolloverMaxIndexDocsCondition;
    }

    public void setRolloverMaxIndexDocsCondition(long rolloverMaxIndexDocsCondition) {
        this.rolloverMaxIndexDocsCondition = rolloverMaxIndexDocsCondition;
    }

    @Override
    public long getRolloverMaxIndexSizeCondition() {
        return rolloverMaxIndexSizeCondition;
    }

    public void setRolloverMaxIndexSizeCondition(long rolloverMaxIndexSizeCondition) {
        this.rolloverMaxIndexSizeCondition = rolloverMaxIndexSizeCondition;
    }

    @Override
    public ByteSizeUnit getRolloverMaxIndexSizeByteSizeUnit() {
        return rolloverMaxIndexSizeByteSizeUnit;
    }

    public void setRolloverMaxIndexSizeByteSizeUnit(ByteSizeUnit rolloverMaxIndexSizeByteSizeUnit) {
        this.rolloverMaxIndexSizeByteSizeUnit = rolloverMaxIndexSizeByteSizeUnit;
    }
}
