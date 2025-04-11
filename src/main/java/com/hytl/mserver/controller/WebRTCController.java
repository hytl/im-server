package com.hytl.mserver.controller;

import com.hytl.mserver.annotation.UserSession;
import com.hytl.mserver.enums.ErrorCode;
import com.hytl.mserver.exception.ImServerException;
import com.hytl.mserver.model.CallSession;
import com.hytl.mserver.model.WsUserSessionMapping;
import com.hytl.mserver.model.message.SignalingMessage;
import com.hytl.mserver.service.CallService;
import com.hytl.mserver.service.MessageService;
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