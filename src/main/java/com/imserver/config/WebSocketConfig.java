package com.imserver.config;

import com.imserver.auth.ws.AuthChannelInterceptor;
import com.imserver.auth.ws.AuthUrlParamHandshakeInterceptor;
import com.imserver.auth.ws.ImServerHandshakeHandler;
import com.imserver.resolver.UserSessionArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

import java.util.List;

@RequiredArgsConstructor
@Configuration(proxyBeanMethods = false)
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 1. 启用简单消息代理
        registry.enableSimpleBroker("/topic", "/queue");

        // 2. 设置应用目的地前缀
        registry.setApplicationDestinationPrefixes("/app");

        // 3. 设置用户目的地前缀
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setHandshakeHandler(new ImServerHandshakeHandler())
                .addInterceptors(new AuthUrlParamHandshakeInterceptor()) // 注册拦截器
                .setAllowedOriginPatterns("*")
        /*.withSockJS().setHeartbeatTime(10000)*/; // 设置 SockJS 客户端心跳间隔为 10 秒
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(64 * 1024) // 设置 传入的 WebSocket 消息的最大大小 为 64 KB（64 * 1024 字节），如果消息超过这个大小，连接将会被关闭
                .setSendBufferSizeLimit(512 * 1024) // 设置 发送 WebSocket 消息的缓冲区最大大小 为 512 KB（512 * 1024 字节），如果消息超过这个大小，连接将会被关闭
                .setSendTimeLimit(10000); // 设置 发送 WebSocket 消息的最大时间限制 为 10 秒（10000 毫秒），如果发送消息的时间超过这个限制，连接可能会被关闭
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        // 注册自定义参数解析器
        argumentResolvers.add(new UserSessionArgumentResolver());
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 注册拦截器
        registration.interceptors(new AuthChannelInterceptor());
    }
}