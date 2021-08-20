package com.zqykj.tldw.aggregate.index.elasticsearch.associate;

import com.zqykj.tldw.aggregate.index.mapping.PersistentEntity;
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
    public void rollover(PersistentEntity<?, ?> persistentEntity, boolean isAsyn) throws Exception;

    /**
     * <h2> 查看indexNames 是否在Elasticsearch 中存在 </h2>
     */
    boolean exists(String... indexNames);

    /**
     * <h2> 当CRUD data 的时候, 如果不手动触发refresh(),默认是根据@Document注解触发 </h2>
     * <p>
     * 手动触发,数据变更能立即搜索到
     * </p>
     */
    void refresh(String... indexNames);
}
