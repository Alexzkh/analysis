/**
 * @作者 Mcj
 */
package com.zqykj.repository.query;

import com.zqykj.core.ElasticsearchRestTemplate;

public class ElasticsearchPartQuery extends AbstractElasticsearchRepositoryQuery {

    public ElasticsearchPartQuery(ElasticsearchQueryMethod queryMethod,
                                  ElasticsearchRestTemplate elasticsearchRestTemplate) {
        super(queryMethod, elasticsearchRestTemplate);
    }

    @Override
    public Object execute(Object[] parameters) {
        return parameters;
    }
}
