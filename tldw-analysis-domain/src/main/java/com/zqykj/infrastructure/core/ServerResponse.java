/**
 * @作者 Mcj
 */
package com.zqykj.infrastructure.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * <h1>统一返回对象</h1>
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
// 保证序列化json的时候,如果是null的对象,key也会消失
@NoArgsConstructor
@Data
public class ServerResponse<T> implements Serializable {

    private int code;
    private String message;
    private T data;

    public ServerResponse(int code) {
        this.code = code;
    }

    public ServerResponse(int code, T data) {
        this.code = code;
        this.data = data;
    }

    public ServerResponse(int code, String msg, T data) {
        this.code = code;
        this.message = msg;
        this.data = data;
    }

    public ServerResponse(int code, String msg) {
        this.code = code;
        this.message = msg;
    }

    public static <T> com.zqykj.infrastructure.core.ServerResponse<T> createBySuccess() {
        return new com.zqykj.infrastructure.core.ServerResponse<T>(ResponseCode.SUCCESS.getCode());
    }

    public static <T> com.zqykj.infrastructure.core.ServerResponse<T> createBySuccessMessage(String msg) {
        return new com.zqykj.infrastructure.core.ServerResponse<T>(ResponseCode.SUCCESS.getCode(), msg);
    }

    public static <T> com.zqykj.infrastructure.core.ServerResponse<T> createBySuccess(T data) {
        return new com.zqykj.infrastructure.core.ServerResponse<T>(ResponseCode.SUCCESS.getCode(), data);
    }

    public static <T> com.zqykj.infrastructure.core.ServerResponse<T> createBySuccess(String msg, T data) {
        return new com.zqykj.infrastructure.core.ServerResponse<T>(ResponseCode.SUCCESS.getCode(), msg, data);
    }

    public static <T> com.zqykj.infrastructure.core.ServerResponse<T> createByError() {
        return new com.zqykj.infrastructure.core.ServerResponse<T>(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getDesc());
    }

    public static <T> com.zqykj.infrastructure.core.ServerResponse<T> createByErrorMessage(String errorMessage) {
        return new com.zqykj.infrastructure.core.ServerResponse<T>(ResponseCode.ERROR.getCode(), errorMessage);
    }

    public static <T> com.zqykj.infrastructure.core.ServerResponse<T> createByErrorCodeMessage(int errorCode, String errorMessage) {
        return new com.zqykj.infrastructure.core.ServerResponse<T>(errorCode, errorMessage);
    }

    public static <T> com.zqykj.infrastructure.core.ServerResponse<T> createByCodeMessage(int code, String message) {
        return new com.zqykj.infrastructure.core.ServerResponse<T>(code, message);
    }

    public static <T> com.zqykj.infrastructure.core.ServerResponse<T> createByCodeDataMessage(int code, String message, T data) {
        return new com.zqykj.infrastructure.core.ServerResponse<T>(code, message, data);
    }

    @JsonIgnore
    // 使之不在json序列化结果当中
    public boolean isSuccess() {
        return this.code == ResponseCode.SUCCESS.getCode();
    }

    public int getCode() {
        return code;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
