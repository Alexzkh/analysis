package com.zqykj.tldw.aggregate.exception;


public class DataOperationClientException extends RuntimeException {
    public DataOperationClientException(String msg) {
        super(msg);
    }

    public DataOperationClientException(String msg, Exception e) {
        super(msg, e);
    }

    public DataOperationClientException(Throwable cause) {
        super(cause);
    }

    public DataOperationClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
