package com.imserver.controller;

import com.imserver.annotation.UserSession;
import com.imserver.model.WsUserSessionMapping;
import com.imserver.model.message.CallHangupMessage;
import com.imserver.model.message.CallRequestMessage;
import com.imserver.model.message.CallResponseMessage;
import com.imserver.service.CallService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

/**
 * 呼叫控制器
 */
@Slf4j
@MessageMapping("/call")
@Controller
@RequiredArgsConstructor
public class CallController {
    private final CallService callService;

    /**
     * 发起呼叫请求
     * 1:N 1个设备可以同时呼叫多个设备，但是只能有一个设备接听
     */
    @MessageMapping("/request")
    public void request(@Valid @Payload CallRequestMessage message, @UserSession WsUserSessionMapping userSessionMapping) {
        message.setCaller(userSessionMapping.userId());

        log.debug("Call-Request sent from {} to {}", message.getCaller(), message.getCallee());

        callService.request(message, userSessionMapping.sessionId());
    }

    /**
     * 响应呼叫请求
     */
    @MessageMapping("/response")
    public void response(@Valid @Payload CallResponseMessage message, @UserSession WsUserSessionMapping userSession) {
        log.debug("Call-Response callId {} type {}", message.getCallId(), message.getType());

        callService.response(message, userSession.sessionId());
    }

    /**
     * 挂断
     */
    @MessageMapping("/hangup")
    public void hangup(@Valid @Payload CallHangupMessage message, @UserSession WsUserSessionMapping userSession) {
        log.debug("Call-Hangup callId {} userId {} sessionId {}", message.getCallId(), userSession.userId(), userSession.sessionId());

        callService.hangup(message);
    }
}