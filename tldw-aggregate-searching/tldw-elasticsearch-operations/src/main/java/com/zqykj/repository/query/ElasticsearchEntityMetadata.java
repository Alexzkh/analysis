/**
 * @作者 Mcj
 */
package com.zqykj.repository.query;

import com.zqykj.repository.core.EntityMetadata;


/**
 * <h1> ElasticsearchEntityMetadata </h1>
 */
public interface ElasticsearchEntityMetadata<T> extends EntityMetadata<T> {

    String getIndexName();
}
