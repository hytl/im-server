package com.imserver.controller;

import com.imserver.annotation.UserSession;
import com.imserver.model.IceServer;
import com.imserver.model.RTCConfiguration;
import com.imserver.model.WsUserSessionMapping;
import com.imserver.service.MessageService;
import com.imserver.util.TurnCredentialsGenerator;
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
