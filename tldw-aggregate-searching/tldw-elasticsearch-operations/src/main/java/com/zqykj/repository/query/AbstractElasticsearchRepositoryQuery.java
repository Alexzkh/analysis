/**
 * @作者 Mcj
 */
package com.zqykj.repository.query;

import com.zqykj.core.ElasticsearchRestTemplate;

/**
 * <h1> AbstractElasticsearchRepositoryQuery </h1>
 */
public abstract class AbstractElasticsearchRepositoryQuery implements RepositoryQuery {

    protected static final int DEFAULT_STREAM_BATCH_SIZE = 500;
    protected ElasticsearchQueryMethod queryMethod;
    protected ElasticsearchRestTemplate elasticsearchOperations;

    public AbstractElasticsearchRepositoryQuery(ElasticsearchQueryMethod queryMethod,
                                                ElasticsearchRestTemplate elasticsearchOperations) {
        this.queryMethod = queryMethod;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @Override
    public QueryMethod getQueryMethod() {
        return queryMethod;
    }
}
