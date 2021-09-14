/**
 * @作者 Mcj
 */
package com.zqykj.repository.query;

import org.springframework.util.Assert;

/**
 * <h1> 对更新结果 DocWriteResponse 的 result 包装 </h1>
 */
public class UpdateResponse {

    private Result result;

    public UpdateResponse(Result result) {

        Assert.notNull(result, "result must not be null");

        this.result = result;
    }

    public Result getResult() {
        return result;
    }

    public enum Result {
        CREATED, UPDATED, DELETED, NOT_FOUND, NOOP;
    }
}
