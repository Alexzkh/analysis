/**
 * @作者 Mcj
 */
package com.zqykj.repository.support;

import com.zqykj.repository.core.EntityInformation;
import org.elasticsearch.index.VersionType;
import org.springframework.lang.Nullable;

/**
 * <h1> Elasticsearch 索引类额外信息接口定义 </h1>
 */
public interface ElasticsearchEntityInformation<T, ID> extends EntityInformation<T, ID> {

    String getIdAttribute();

    String getIndex();

    @Nullable
    Long getVersion(T entity);

    @Nullable
    VersionType getVersionType();
}
