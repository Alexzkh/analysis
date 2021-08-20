package com.zqykj.tldw.aggregate.index.elasticsearch.index;

import com.zqykj.tldw.aggregate.index.mapping.PersistentEntity;
import com.zqykj.tldw.aggregate.index.operation.IndexOperations;

/**
 * @Description: abstract elasticseach operations .
 * @Author zhangkehou
 * @Date 2021/8/16
 */
public interface ElasticsearchIndexOperate extends IndexOperations {

    /**
     * Rolling index
     *
     * @param isAsyn Asynchronous or not
     * @throws Exception
     */
    public void rollover(PersistentEntity<?, ?> persistentEntity, boolean isAsyn) throws Exception;

    /**
     * Get index name
     *
     * @return
     */
    public String getIndexName();
}
