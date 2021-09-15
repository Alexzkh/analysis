/**
 * @作者 Mcj
 */
package com.zqykj.exception;


import org.springframework.dao.UncategorizedDataAccessException;

public class UncategorizedElasticsearchException extends UncategorizedDataAccessException {

    public UncategorizedElasticsearchException(String msg) {
        super(msg, null);
    }

    public UncategorizedElasticsearchException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
