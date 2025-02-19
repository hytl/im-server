package com.imserver.enums;

/**
 * 响应码
 */
public enum ErrorCode {
    // 通用
    OK(200, "ok"),
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),

    // 业务 1000以上
    CALL_USER_BUSY(1001, "User Busy"); // 用户繁忙（占线）
    ;
    private Integer code;
    private String msg;

    ErrorCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
