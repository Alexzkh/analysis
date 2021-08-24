/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.exception;

import org.springframework.dao.DataAccessException;

public class UncategorizedElasticsearchException extends DataAccessException {

    public UncategorizedElasticsearchException(String msg) {
        super(msg, null);
    }

    public UncategorizedElasticsearchException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
