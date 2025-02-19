package com.imserver.resolver;

import com.imserver.annotation.UserSession;
import com.imserver.context.UserSessionContext;
import org.springframework.core.MethodParameter;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

public class UserSessionArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(UserSession.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, Message<?> message) throws Exception {
        // 从消息头中获取会话属性
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(message);
        String sessionId = accessor.getSessionId();

        return UserSessionContext.getWsUserSessionInfo(sessionId);
    }
}