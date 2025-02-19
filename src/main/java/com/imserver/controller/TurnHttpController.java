package com.imserver.controller;

import com.imserver.annotation.UserId;
import com.imserver.model.IceServer;
import com.imserver.model.RTCConfiguration;
import com.imserver.model.RestResult;
import com.imserver.service.MessageService;
import com.imserver.util.TurnCredentialsGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TURN控制器
 */
@Slf4j
@Validated
@RequestMapping("/turn")
@RestController
public class TurnHttpController {
    private final MessageService messageService;

    private final String stunServer;

    private final String turnServer;

    private final String sharedSecret;

    private final int turnValidityDuration;

    public TurnHttpController(MessageService messageService, @Value("${stun.server}") String stunServer
            , @Value("${turn.server}") String turnServer, @Value("${turn.shared.secret}") String sharedSecret
            , @Value("${turn.validity.duration:7200}") int turnValidityDuration) {
        this.messageService = messageService;
        this.stunServer = stunServer;
        this.turnServer = turnServer;
        this.sharedSecret = sharedSecret;
        this.turnValidityDuration = turnValidityDuration;
    }

    @GetMapping("/get-turn-credentials")
    public RestResult<RTCConfiguration> getTurnCredentials(@UserId String userId) {
        // 创建 ICE 服务器配置
        IceServer stunIceServer = new IceServer("stun:" + stunServer, null, null);

        String username = TurnCredentialsGenerator.generateUsername(userId, turnValidityDuration);
        IceServer turnIceServer = new IceServer("turns:" + turnServer
                , username, TurnCredentialsGenerator.generatePassword(username, sharedSecret));

        // 创建 RTCConfiguration
        RTCConfiguration config = new RTCConfiguration();
        config.addIceServer(stunIceServer);
        config.addIceServer(turnIceServer);

        return RestResult.ok(config);
    }
}
