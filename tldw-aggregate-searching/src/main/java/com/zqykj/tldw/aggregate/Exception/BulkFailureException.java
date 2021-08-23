/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.Exception;

import java.util.Map;

/**
 * <h2> 索引批量操作失败 </h2>
 */
public class BulkFailureException extends RuntimeException {

    private final Map<String, String> failedDocuments;

    public BulkFailureException(String msg, Map<String, String> failedDocuments) {
        super(msg);
        this.failedDocuments = failedDocuments;
    }

    public Map<String, String> getFailedDocuments() {
        return failedDocuments;
    }
}
