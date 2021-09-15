/**
 * @作者 Mcj
 */
package com.zqykj.exception;

import org.springframework.dao.DataRetrievalFailureException;

import java.util.Map;

public class BulkFailureException extends DataRetrievalFailureException {

    private final Map<String, String> failedDocuments;

    public BulkFailureException(String msg, Map<String, String> failedDocuments) {
        super(msg);
        this.failedDocuments = failedDocuments;
    }

    public Map<String, String> getFailedDocuments() {
        return failedDocuments;
    }
}
