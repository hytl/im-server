package com.imserver.auth.ws;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.security.Principal;
import java.util.Map;

public class AuthUrlParamHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            // 由于SockJs一些客户端的限制，客户端在链接处无法传递自定义header，但是为了校验用户信息，提前在链接处传递userId，
            // 在STOMP CONNECT帧校验token，token校验判断userId是否一致
            String userId = servletRequest.getServletRequest().getParameter("userId");

            if (userId == null) return false;

            final String userIdTrim = userId.trim();

            // 创建 Principal 对象
            Principal principal = () -> userIdTrim;
            // 将 Principal 存储到 attributes 中
            attributes.put("principal", principal);
            return true; // 允许握手
        }
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return false; // 拒绝握手
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        //  // 握手后的逻辑（可选）
    }
}