package com.hytl.mserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * 消息服务
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class MessageService {
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 发送给用户所有在线客户端
     *
     * @param userId      用户ID 必填
     * @param destination 发送消息的目的地 必填
     * @param payload     发送信息载体 可选
     */
    public void sendToUserAllOnlineClients(String userId, String destination, Object payload) {
        messagingTemplate.convertAndSendToUser(userId, destination, payload);
    }

    /**
     * 发送给用户指定客户端
     *
     * @param sessionId   sessionId
     * @param destination 发送消息的目的地 必填
     * @param payload     发送信息载体 可选
     */
    public void sendToUserClient(String sessionId, String destination, Object payload) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create();
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        messagingTemplate.convertAndSendToUser(sessionId, destination, payload, headerAccessor.getMessageHeaders());
    }

    /**
     * 发送给所有订阅了该主题的用户
     *
     * @param topic   主题名称
     * @param payload 发送信息载体 可选
     */
    public void sendToTopic(String topic, Object payload) {
        messagingTemplate.convertAndSend(topic, payload);
    }
}
