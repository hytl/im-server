package com.imserver.controller;

import com.imserver.annotation.UserSession;
import com.imserver.model.UserStatus;
import com.imserver.model.WsUserSessionMapping;
import com.imserver.service.MessageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户控制器
 */
@Slf4j
@Validated
@MessageMapping("/user")
@Controller
@RequiredArgsConstructor
public class UserController {
    private final MessageService messageService;
    private final SimpUserRegistry simpUserRegistry;

    /**
     * 查询用户状态
     */
    @MessageMapping("/status")
    public void userStatus(@Valid @Size(max = 200) @Payload Set<String> userIds, @UserSession WsUserSessionMapping userSession) {
        if (CollectionUtils.isEmpty(userIds)) {
            return;
        }

        // 遍历该用户的所有会话
        List<UserStatus> userStatusMessageList = userIds.stream().map(userId -> {
            SimpUser simpUser = simpUserRegistry.getUser(userId);
            if (simpUser == null || CollectionUtils.isEmpty(simpUser.getSessions())) {
                return new UserStatus(userId, UserStatus.Status.OFFLINE);
            }
            return new UserStatus(userId, UserStatus.Status.ONLINE);
        }).collect(Collectors.toList());

        messageService.sendToTopic("/topic/user/status", userStatusMessageList);


        log.debug("User-Status userId {} sessionId {}", userSession.userId(), userSession.sessionId());
    }
}
