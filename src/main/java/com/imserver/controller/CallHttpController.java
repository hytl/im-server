package com.imserver.controller;

import com.imserver.annotation.UserId;
import com.imserver.annotation.WsSessionId;
import com.imserver.enums.ErrorCode;
import com.imserver.exception.ImServerException;
import com.imserver.model.RestResult;
import com.imserver.model.message.CallRequestMessage;
import com.imserver.model.message.CallResponseMessage;
import com.imserver.service.CallService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 呼叫控制器
 */
@Slf4j
@RequestMapping("/call")
@RestController
@RequiredArgsConstructor
public class CallHttpController {
    private final CallService callService;

    /**
     * 发起呼叫请求
     * 1:N 1个设备可以同时呼叫多个设备，但是只能有一个设备接听
     */
    @PostMapping("/request")
    public RestResult request(@Valid @RequestBody CallRequestMessage message, @UserId String userId
            , @WsSessionId String wsSessionId) {
        if (wsSessionId == null) {
            throw new ImServerException(ErrorCode.FORBIDDEN, "The websocket connection is not established.");
        }

        message.setCaller(userId);

        log.debug("Call-Request sent from {} to {}", message.getCaller(), message.getCallee());

        callService.request(message, wsSessionId);

        return RestResult.ok();
    }

    /**
     * 响应呼叫请求
     */
    @PostMapping("/response")
    public RestResult response(@Valid @RequestBody CallResponseMessage message, @UserId String userId
            , @WsSessionId String wsSessionId) {
        if (wsSessionId == null) {
            throw new ImServerException(ErrorCode.FORBIDDEN, "The websocket connection is not established.");
        }

        log.debug("Call-Response callId {} type {}", message.getCallId(), message.getType());

        callService.response(message, wsSessionId);

        return RestResult.ok();
    }

    /**
     * 挂断
     */
    @PostMapping("/hangup")
    public RestResult hangup(@UserId String userId, @WsSessionId String wsSessionId) {
        if (wsSessionId == null) {
            throw new ImServerException(ErrorCode.FORBIDDEN, "The websocket connection is not established.");
        }

        log.debug("Call-Hangup userId {} sessionId {}", userId, wsSessionId);

        callService.hangup(wsSessionId);

        return RestResult.ok();
    }
}