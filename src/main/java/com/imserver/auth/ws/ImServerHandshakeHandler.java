package com.imserver.auth.ws;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

public class ImServerHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        // 从 attributes 中提取 Principal
        Principal principal = (Principal) attributes.get("principal");
        if (principal != null) {
            return principal;
        }
        return super.determineUser(request, wsHandler, attributes);
    }
}