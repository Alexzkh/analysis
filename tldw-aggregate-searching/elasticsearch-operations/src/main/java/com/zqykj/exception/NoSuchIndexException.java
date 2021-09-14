/**
 * @作者 Mcj
 */
package com.zqykj.exception;

import org.springframework.dao.NonTransientDataAccessResourceException;

public class NoSuchIndexException extends NonTransientDataAccessResourceException {

    private final String index;

    public NoSuchIndexException(String index, Throwable cause) {
        super(String.format("Index %s not found.", index), cause);
        this.index = index;
    }

    public String getIndex() {
        return index;
    }
}
