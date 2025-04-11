package com.hytl.mserver.exception;

import com.hytl.mserver.enums.ErrorCode;
import com.hytl.mserver.model.RestResult;
import com.hytl.mserver.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@RequiredArgsConstructor
@ControllerAdvice
@Slf4j
public class WebGlobalExceptionHandler {
    private final MessageService messageService;

    @ExceptionHandler
    public ResponseEntity<RestResult> handleImServerException(ImServerException ex) {
        log.error("ImServerException error: {} userId: {} sessionId: {}", ex.getMessage());
        int code = ex.getCode();
        int returnHttpStatusCode = code > 1000 ? 200 : code;
        return new ResponseEntity<>(new RestResult(code, ex.getMessage(), null), HttpStatusCode.valueOf(returnHttpStatusCode));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestResult> handleValidationException(MethodArgumentNotValidException ex) {
        log.error("MethodArgumentNotValidException error: {}", ex.getMessage());
        return new ResponseEntity<>(new RestResult<>(ErrorCode.BAD_REQUEST
                , ex.getBindingResult() != null ? ex.getBindingResult().getAllErrors().get(0).toString() : null)
                , HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<RestResult> handleException(Exception ex) {
        log.error("Unexpected error", ex);
        // 处理其他异常
        return new ResponseEntity<>(new RestResult<>(ErrorCode.INTERNAL_SERVER_ERROR, ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}