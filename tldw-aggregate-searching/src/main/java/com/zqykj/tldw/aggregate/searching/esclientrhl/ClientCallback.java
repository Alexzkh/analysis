package com.zqykj.tldw.aggregate.searching.esclientrhl;

import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;

/**
 * @Description: Callback interface to be used with execute(ClientCallback) for operating directly on
 * 	 * {@link RestHighLevelClient}.
 * @Author zhangkehou
 * @Date 2021/8/19
 */
@FunctionalInterface
public interface ClientCallback<T> {
    T doWithClient(RestHighLevelClient client) throws IOException;
}
