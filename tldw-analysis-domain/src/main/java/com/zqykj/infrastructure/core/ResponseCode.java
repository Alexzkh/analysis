package com.zqykj.infrastructure.core;

/**
 * <h1>统一响应码</h1>
 */
public enum ResponseCode {

    /**
     * 成功返回
     */
    SUCCESS(200, "SUCCESS"),
    /**
     * 错误返回
     */
    ERROR(10001, "ERROR"), NEED_LOGIN(10002, "NEED_LOGIN"),
    /**
     * 未登录
     */
    ILLEGAL_ARGUMENT(10003, "ILLEGAL_ARGUMENT"),

    /** 资金战法*/
    ACCESS_DATA(30001,"数据获取失败"),

    /**
     * 请求服务异常
     */
    SERVER_ERROR(500, "Server Error");

    private final int code;
    private final String desc;

    ResponseCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

}
