package com.imserver.auth.ws;

import com.imserver.context.UserSessionContext;
import com.imserver.enums.ErrorCode;
import com.imserver.exception.ImServerException;
import com.imserver.model.UserInfo;
import com.imserver.util.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;

import java.security.Principal;

@Slf4j
public class AuthChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        Principal principal = accessor.getUser();
        String sessionId = accessor.getSessionId();
        if (principal == null) {
            throw new ImServerException(ErrorCode.FORBIDDEN, "No authentication information");
        }

        String userId = principal.getName();

        // 检查是否是 CONNECT 帧
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 验证Token
            String token = accessor.getFirstNativeHeader(AuthUtil.HEADER_NAME_TOKEN);
            if (token == null) {
                // 抛出异常，关闭连接
                throw new ImServerException(ErrorCode.FORBIDDEN, "Authentication failure: No token");
            }

            UserInfo userInfo = UserSessionContext.getUserInfoByToken(token);
            if (userInfo == null) {
                // TODO TOKEN校验
                UserInfo userInfoByToken = null;
                try {
                    userInfoByToken = null;
                } catch (Exception e) {
                    log.error("Authentication failure: fail to get token info", e);
                    throw new ImServerException(ErrorCode.FORBIDDEN, "Authentication failure: fail to get token info");
                }

                if (userInfoByToken == null) {
                    // 抛出异常，关闭连接
                    throw new ImServerException(ErrorCode.FORBIDDEN, "Authentication failure: No userInfo");
                }

                if (!userId.equals(userInfoByToken.userId())) {
                    throw new ImServerException(ErrorCode.FORBIDDEN, "Authentication failure: The userid is different");
                }

                UserSessionContext.online(token, userInfoByToken.userId(), userInfoByToken.username());
            }
            UserSessionContext.putWsSessionIdToken(sessionId, token);
        }

        return message;
    }
}