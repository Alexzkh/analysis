package com.zqykj.common.core;

/**
 * <h1>统一响应码</h1>
 */
public enum ResponseCode {

    /**
     * 成功返回
     */
    SUCCESS(0, "SUCCESS"),
    /**
     * 错误返回
     */
    ERROR(10001, "ERROR"), NEED_LOGIN(10002, "NEED_LOGIN"),
    /**
     * 未登录
     */
    ILLEGAL_ARGUMENT(10003, "ILLEGAL_ARGUMENT"),

    /** 成功系统对接部分 */
    /**
     * token认证失败
     */
    UN_AUTHENTICATED(20001, "UN_AUTHENTICATED"),
    /**
     * 用户账号已经存在
     */
    USER_EXIST(20002, "USER_EXIST"),
    /**
     * 系统错误
     */
    SYSTEM_ERROR(20003, "SYSTEM_ERROR"),
    /**
     * 连接超时
     */
    CONNECT_TIME_OUT(20004, "CONNECT_TIME_OUT"),
    /**
     * 返回用户信息为空,认证失败
     */
    USER_INFO_EMPTY(20005, "USER_INFO_EMPTY"),
    /**
     * 用户不存在
     */
    USER_NOT_EXIST(20006, "USER_NOT_EXIST"),
    /**
     * 参数错误
     */
    PARAM_ERROR(20007, "PARAM_ERROR"),
    /**
     * 请求接口错误,返回的状态码不是200
     */
    REQUEST_ERROR(20008, "REQUEST_ERROR");
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
