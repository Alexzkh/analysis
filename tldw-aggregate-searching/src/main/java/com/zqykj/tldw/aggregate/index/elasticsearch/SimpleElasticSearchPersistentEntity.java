/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.index.elasticsearch;

import com.zqykj.infrastructure.util.TypeInformation;
import com.zqykj.tldw.aggregate.index.mapping.BasicPersistentEntity;
import lombok.Getter;
import lombok.Setter;
import org.elasticsearch.common.collect.MapBuilder;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.index.VersionType;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.zqykj.annotations.Document;

/**
 * <h1> Elasticsearch persistent entity information describe,the notes {@link Document}</h1>
 */
@Setter
@Getter
public class SimpleElasticSearchPersistentEntity<T> extends BasicPersistentEntity<T, SimpleElasticSearchPersistentProperty> {

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
