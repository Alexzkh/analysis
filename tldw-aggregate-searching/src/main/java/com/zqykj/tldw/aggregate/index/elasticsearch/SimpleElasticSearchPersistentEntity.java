/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.index.elasticsearch;

import com.zqykj.annotations.Setting;
import com.zqykj.infrastructure.util.TypeInformation;
import com.zqykj.tldw.aggregate.index.mapping.BasicPersistentEntity;
import com.zqykj.tldw.aggregate.index.mapping.PropertyHandler;
import lombok.Getter;
import lombok.Setter;
import org.elasticsearch.common.collect.MapBuilder;
import org.elasticsearch.index.VersionType;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import com.zqykj.annotations.Document;

/**
 * <h1> Elasticsearch persistent entity information describe</h1>
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
