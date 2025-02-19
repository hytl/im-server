package com.imserver.controller;

import com.imserver.annotation.UserSession;
import com.imserver.enums.ErrorCode;
import com.imserver.exception.ImServerException;
import com.imserver.model.CallSession;
import com.imserver.model.WsUserSessionMapping;
import com.imserver.model.message.SignalingMessage;
import com.imserver.service.CallService;
import com.imserver.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

/**
 * WebRTC信令控制器
 */
@Slf4j
@Validated
@MessageMapping("/rtc")
@Controller
@RequiredArgsConstructor
public class WebRTCController {
    private final CallService callService;
    private final MessageService messageService;

    /**
     * 处理信令消息，并将其发送给指定的接收者
     * <pre>
     * +---------+       +---------+       +---------+
     * | ClientA |       | Signaling |     | ClientB |
     * +---------+       |  Server  |     +---------+
     *     |                 |                 |
     *     |--- Offer SDP --->|                 |
     *     |                 |--- Offer SDP --->|
     *     |                 |<--- Answer SDP --|
     *     |<-- Answer SDP --|                 |
     *     |                 |                 |
     *     |--- ICE Candidate -->|             |
     *     |                 |--- ICE Candidate -->|
     *     |                 |<-- ICE Candidate --|
     *     |<-- ICE Candidate --|             |
     *     |                 |                 |
     *     |                 |                 |
     *     |<== P2P Connection Established ==>|
     *     |                 |                 |
     * </pre>
     */
    @MessageMapping("/signaling")
    public void handleSignal(@UserSession WsUserSessionMapping userSessionMapping, @Valid @Payload SignalingMessage message) {
        CallSession callSession = callService.getCallSession(message.getCallId());

        if (callSession == null || CallSession.CallStatus.ENDED == callSession.getStatus()
                || CallSession.CallStatus.REJECTED == callSession.getStatus()) {
            throw new ImServerException(ErrorCode.INTERNAL_SERVER_ERROR, "呼叫状态异常");
        }

        String callerSessionId = callSession.getCallerSessionId();
        String calleeSessionId = callSession.getCalleeSessionId();

        boolean isCaller = callerSessionId.equals(userSessionMapping.sessionId());
        // 发送消息是发送给对方
        messageService.sendToUserClient(isCaller ? calleeSessionId : callerSessionId, "/queue/rtc/signaling", message);

        log.debug("WebRTC-Signal sent type {} from {}", message.getType(), isCaller ? callSession.getCaller() : callSession.getCallee());
    }
}