package com.hytl.mserver.controller;

import com.hytl.mserver.annotation.UserSession;
import com.hytl.mserver.model.IceServer;
import com.hytl.mserver.model.RTCConfiguration;
import com.hytl.mserver.model.WsUserSessionMapping;
import com.hytl.mserver.service.MessageService;
import com.hytl.mserver.util.TurnCredentialsGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

/**
 * TURN控制器
 */
@Slf4j
@Validated
@MessageMapping("/turn")
@Controller
public class TurnController {
    private final MessageService messageService;

    private final String stunServer;

    private final String turnServer;

    private final String sharedSecret;

    private final int turnValidityDuration;

    public TurnController(MessageService messageService, @Value("${stun.server}") String stunServer
            , @Value("${turn.server}") String turnServer, @Value("${turn.shared.secret}") String sharedSecret
            , @Value("${turn.validity.duration:7200}") int turnValidityDuration) {
        this.messageService = messageService;
        this.stunServer = stunServer;
        this.turnServer = turnServer;
        this.sharedSecret = sharedSecret;
        this.turnValidityDuration = turnValidityDuration;
    }

    @MessageMapping("/get-turn-credentials")
    public void request(@UserSession WsUserSessionMapping userSessionMapping) {
        String userId = userSessionMapping.userId();
        // 创建 ICE 服务器配置
        IceServer stunIceServer = new IceServer("stun:" + stunServer, null, null);

        String username = TurnCredentialsGenerator.generateUsername(userId, turnValidityDuration);
        IceServer turnIceServer = new IceServer("turns:" + turnServer
                , username, TurnCredentialsGenerator.generatePassword(username, sharedSecret));

        // 创建 RTCConfiguration
        RTCConfiguration config = new RTCConfiguration();
        config.addIceServer(stunIceServer);
        config.addIceServer(turnIceServer);

        messageService.sendToUserClient(userSessionMapping.sessionId(), "/queue/turn/get-turn-credentials"
                , config);
    }
}
