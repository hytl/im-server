package com.imserver.exception;

import com.imserver.enums.ErrorCode;

public class ImServerException extends RuntimeException {
    private int code;

    public ImServerException(ErrorCode errorCode) {
        super(errorCode.getMsg());
    }

    public ImServerException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMsg(), cause);
        this.code = errorCode.getCode();
    }

    public ImServerException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    public ImServerException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.code = errorCode.getCode();
    }

    public int getCode() {
        return code;
    }
}