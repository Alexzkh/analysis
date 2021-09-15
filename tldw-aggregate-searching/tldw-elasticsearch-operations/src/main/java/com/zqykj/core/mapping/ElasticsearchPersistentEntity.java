/**
 * @作者 Mcj
 */
package com.zqykj.core.mapping;

import com.zqykj.core.document.Document;
import com.zqykj.domain.Routing;
import com.zqykj.mapping.PersistentEntity;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.index.VersionType;
import org.springframework.lang.Nullable;

import java.util.concurrent.TimeUnit;

/**
 * <h1> Elasticsearch Persistent Entity describe</h1>
 * <p>
 * 这里会定义一些Elasticsearch persistent entity 特有属性方法获取
 * </p>
 */
public interface ElasticsearchPersistentEntity<T> extends PersistentEntity<T, ElasticsearchPersistentProperty> {

    String getIndexName();

    short getShards();

    short getReplicas();

    boolean isUseServerConfiguration();

    @Nullable
    String getRefreshInterval();

    @Nullable
    String getIndexStoreType();

    ElasticsearchPersistentProperty getVersionProperty();

    @Nullable
    String settingPath();

    @Nullable
    VersionType getVersionType();

    boolean isCreateIndexAndMapping();

    boolean isRollover();

    boolean isAutoRollover();

    long getAutoRolloverInitialDelay();

    long getAutoRolloverPeriod();

    TimeUnit getAutoRolloverTimeUnit();

    long getRolloverMaxIndexAgeCondition();

    TimeUnit getRolloverMaxIndexAgeTimeUnit();

    long getRolloverMaxIndexDocsCondition();

    long getRolloverMaxIndexSizeCondition();

    ByteSizeUnit getRolloverMaxIndexSizeByteSizeUnit();

    @Nullable
    ElasticsearchPersistentProperty getPersistentPropertyWithFieldName(String fieldName);

    Document getDefaultSettings();

    void setIndexName(String indexName);

    ElasticsearchPersistentProperty getRoutingFieldProperty();
}
