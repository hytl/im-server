package com.imserver.model;

import com.imserver.enums.ErrorCode;
import lombok.Data;

/**
 * 响应结果
 */
@Data
public class RestResult<D> {
    private Integer code;
    private String msg;
    private D data;

    private static RestResult REST_RESULT_OK = new RestResult(ErrorCode.OK);

    public RestResult(Integer code, String msg, D data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public RestResult(ErrorCode restCode) {
        this.code = restCode.getCode();
        this.msg = restCode.getMsg();
    }

    public RestResult(ErrorCode restCode, String msg) {
        this.code = restCode.getCode();
        this.msg = msg;
    }

    public static RestResult ok(Object data) {
        return new RestResult<>(ErrorCode.OK.getCode(), ErrorCode.OK.getMsg(), data);
    }

    public static RestResult ok() {
        return REST_RESULT_OK;
    }
}
