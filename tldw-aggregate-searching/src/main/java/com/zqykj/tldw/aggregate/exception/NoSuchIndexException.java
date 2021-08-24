/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.exception;

import org.springframework.dao.DataAccessException;

public class NoSuchIndexException extends DataAccessException {

    private final String index;

    public NoSuchIndexException(String index, Throwable cause) {
        super(String.format("Index %s not found.", index), cause);
        this.index = index;
    }

    public String getIndex() {
        return index;
    }
}
