package com.zqykj.tldw.aggregate.index.elasticsearch.index;

import com.zqykj.tldw.aggregate.index.operation.IndexOperations;

/**
 * @Description: TODO
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
    public void rollover( boolean isAsyn) throws Exception;

    /**
     * Get index name
     *
     * @return
     */
    public String getIndexName();
}
