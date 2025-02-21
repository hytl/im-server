package com.imserver.listener;

import com.imserver.context.UserSessionContext;
import com.imserver.model.UserInfo;
import com.imserver.model.UserStatus;
import com.imserver.service.CallService;
import com.imserver.util.ThreadUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Collections;
import java.util.Optional;

/**
 * Spring 提供了以下 WebSocket 事件，可以通过 @EventListener 注解监听：
 * <p>
 * SessionConnectedEvent：WebSocket 连接建立时触发。
 * <p>
 * SessionDisconnectEvent：WebSocket 连接断开时触发。
 * <p>
 * SessionSubscribeEvent：客户端订阅某个目的地时触发。
 * <p>
 * SessionUnsubscribeEvent：客户端取消订阅时触发。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SessionEventListener {
    private final SimpMessagingTemplate messagingTemplate;
    private final CallService callService;
    private final ThreadUtil threadUtil;
    private final SimpUserRegistry simpUserRegistry;

    @EventListener
    public void handleConnectedListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String userId = event.getUser().getName();
        String sessionId = headerAccessor.getSessionId();

        UserSessionContext.wsSessionConnect(userId, sessionId);

        // 广播用户在线消息
        threadUtil.execute(() -> messagingTemplate.convertAndSend("/topic/user/status"
                , Collections.singleton(new UserStatus(userId, UserStatus.Status.ONLINE))));

        String username = Optional.ofNullable(UserSessionContext.getUserInfoByUserId(userId)).map(UserInfo::username).orElse(null);

        log.info("SessionConnectedEvent: 用户SESSION_ID->{} USER_ID->{} USER_NAME->{} 上线", sessionId, userId, username);
    }

    /**
     * SessionDisconnectEvent 是在 WebSocket 会话断开时触发的事件。它可以由以下原因触发：
     * 1.客户端主动发送 DISCONNECT 帧。
     * 2.客户端直接关闭 WebSocket 连接（例如关闭浏览器标签或调用 websocket.close()）。
     * 3.服务器主动关闭连接（例如由于心跳超时或异常）。
     * 4.网络问题导致连接中断。
     */
    @EventListener
    public void handleDisconnectListener(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        String userId = event.getUser().getName();

        threadUtil.execute(() -> UserSessionContext.wsSessionDisconnect(sessionId));
        threadUtil.execute(() -> callService.cleanByUserId(userId));

        // 广播用户离线消息
        threadUtil.execute(() -> messagingTemplate.convertAndSend("/topic/user/status"
                , Collections.singleton(new UserStatus(userId, UserStatus.Status.OFFLINE))));

        String username = Optional.ofNullable(UserSessionContext.getUserInfoByUserId(userId)).map(UserInfo::username).orElse(null);

        UserSessionContext.offline(userId, sessionId);

        log.info("SessionDisconnectEvent: 用户SESSION_ID->{} USER_ID->{} USER_NAME->{} 离线", sessionId, userId, username);
    }

}