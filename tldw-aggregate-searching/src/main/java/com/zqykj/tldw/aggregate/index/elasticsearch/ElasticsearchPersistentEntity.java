/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.index.elasticsearch;

import com.zqykj.tldw.aggregate.index.mapping.PersistentEntity;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.index.VersionType;
import org.springframework.lang.Nullable;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <h1> Elasticsearch Persistent Entity describe</h1>
 * <p>
 * 这里会定义一些Elasticsearch persistent entity 特有属性方法获取
 * </p>
 */
public interface ElasticsearchPersistentEntity<T> extends PersistentEntity<T, SimpleElasticSearchPersistentProperty> {

    String getIndexName();

    short getShards();

    short getReplicas();

    boolean isUseServerConfiguration();

    @Nullable
    String getRefreshInterval();

    @Nullable
    String getIndexStoreType();

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
    SimpleElasticSearchPersistentProperty getPersistentPropertyWithFieldName(String fieldName);

    Map<String, ?> getDefaultSettings();
}
