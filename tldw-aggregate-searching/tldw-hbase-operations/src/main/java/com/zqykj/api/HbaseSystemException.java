package com.zqykj.api;

/**
 * HBase Data Access exception.
 *
 * @author Costin Leau
 */

import org.springframework.dao.UncategorizedDataAccessException;

/**
 *
 * desc： copy from spring data hadoop hbase, modified by JThink
 */
public class HbaseSystemException extends UncategorizedDataAccessException {

    private static final long serialVersionUID = 5868772545179711372L;

    public HbaseSystemException(Exception cause) {
        super(cause.getMessage(), cause);
    }

    public HbaseSystemException(Throwable throwable) {
        super(throwable.getMessage(), throwable);
    }
}
