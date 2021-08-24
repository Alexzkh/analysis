package com.zqykj.infrastructure.exception;


/**
 * KV storage exception .
 */
public class KvStorageException extends AnalysisException {

    public KvStorageException() {
        super();
    }

    public KvStorageException(ErrorCode code, String errMsg) {
        super(code.getCode(), errMsg);
    }

    public KvStorageException(ErrorCode errCode, Throwable throwable) {
        super(errCode.getCode(), throwable);
    }

    public KvStorageException(ErrorCode errCode, String errMsg, Throwable throwable) {
        super(errCode.getCode(), errMsg, throwable);
    }

    public KvStorageException(int errCode, String errMsg) {
        super(errCode, errMsg);
    }

    public KvStorageException(int errCode, Throwable throwable) {
        super(errCode, throwable);
    }

    public KvStorageException(int errCode, String errMsg, Throwable throwable) {
        super(errCode, errMsg, throwable);
    }

    @Override
    public int getErrCode() {
        return super.getErrCode();
    }

    @Override
    public String getErrMsg() {
        return super.getErrMsg();
    }

    @Override
    public void setErrCode(int errCode) {
        super.setErrCode(errCode);
    }

    @Override
    public void setErrMsg(String errMsg) {
        super.setErrMsg(errMsg);
    }

    @Override
    public void setCauseThrowable(Throwable throwable) {
        super.setCauseThrowable(throwable);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
