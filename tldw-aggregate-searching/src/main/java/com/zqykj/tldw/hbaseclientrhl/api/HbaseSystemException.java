package com.zqykj.tldw.hbaseclientrhl.api;

import org.springframework.dao.UncategorizedDataAccessException;

/**
 * HBase Data Access exception.
 *
 * @author Costin Leau
 */

/**
 *
 * desc： copy from spring data hadoop hbase, modified by JThink
 */
public class HbaseSystemException extends UncategorizedDataAccessException {

    public HbaseSystemException(Exception cause) {
        super(cause.getMessage(), cause);
    }

    public HbaseSystemException(Throwable throwable) {
        super(throwable.getMessage(), throwable);
    }
}
