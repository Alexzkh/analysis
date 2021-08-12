package com.zqykj.tldw.aggregate.Exception;


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
