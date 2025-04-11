package com.hytl.mserver.exception;

import com.hytl.mserver.enums.ErrorCode;
import com.hytl.mserver.model.ErrorResponse;
import com.hytl.mserver.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@RequiredArgsConstructor
@ControllerAdvice
@Slf4j
public class WsGlobalExceptionHandler {
    private final MessageService messageService;

    @ExceptionHandler
    public void handleImServerException(ImServerException ex, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        String userId = headerAccessor.getUser().getName();
        messageService.sendToUserClient(sessionId, "/queue/errors"
                , new ErrorResponse(ex.getCode(), ex.getMessage()));
        log.error("ImServerException error: {} userId: {} sessionId: {}", ex.getMessage(), userId, sessionId);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public void handleValidationException(MethodArgumentNotValidException ex, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        String userId = headerAccessor.getUser().getName();

        messageService.sendToUserClient(sessionId, "/queue/errors"
                , new ErrorResponse(ErrorCode.BAD_REQUEST.getCode()
                , ex.getBindingResult() != null ? ex.getBindingResult().getAllErrors().get(0).toString() : null));
        log.error("MethodArgumentNotValidException error: {} userId: {} sessionId: {}", ex.getMessage(), userId, sessionId);
    }

    @ExceptionHandler
    public void handleException(Exception ex, SimpMessageHeaderAccessor headerAccessor) {
        log.error("Unexpected error: {}", ex.getMessage());
        // 处理其他异常
        String sessionId = headerAccessor.getSessionId();
        String userId = headerAccessor.getUser().getName();
        messageService.sendToUserClient(sessionId, "/queue/errors"
                , new ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR.getCode()
                , ex.getClass() + ":" + ex.getMessage()));
        log.error("Exception userId: {} sessionId: {}", userId, sessionId, ex);
    }
}