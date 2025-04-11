package com.hytl.mserver.config;

import com.hytl.mserver.auth.ws.AuthChannelInterceptor;
import com.hytl.mserver.auth.ws.AuthUrlParamHandshakeInterceptor;
import com.hytl.mserver.auth.ws.ImServerHandshakeHandler;
import com.hytl.mserver.resolver.UserSessionArgumentResolver;
import jakarta.annotation.PostConstruct;
import jakarta.websocket.ContainerProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

import java.util.List;

@RequiredArgsConstructor
@Configuration(proxyBeanMethods = false)
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    /**
     * WebSocket最大会话空闲超时时间
     * 缺省：20000 单位：ms
     * 小于等于0则为不限
     */
    @Value("${websocket.max.session.idle.timeout:20000}")
    private int websocketMaxSessionIdleTimeout;

    @PostConstruct
    public void setIdleTimeout() {
        ContainerProvider.getWebSocketContainer().setDefaultMaxSessionIdleTimeout(websocketMaxSessionIdleTimeout);
    }

    @Bean
    public TaskScheduler websocketHeartbeatTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(Runtime.getRuntime().availableProcessors() * 2);
        scheduler.setThreadNamePrefix("websocket-heartbeat-");
        scheduler.initialize();
        return scheduler;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 1. 启用简单消息代理
        registry.enableSimpleBroker("/topic", "/queue")
                .setHeartbeatValue(new long[]{10000, 10000})
                .setTaskScheduler(websocketHeartbeatTaskScheduler()); // [服务端发送心跳间隔, 期望客户端心跳间隔]

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
        registration.setMessageSizeLimit(1024 * 1024) // 设置 传入的 WebSocket 消息的最大大小 为 1MB（1024 * 1024 字节），如果消息超过这个大小，连接将会被关闭
                .setSendBufferSizeLimit(1024 * 1024) // 设置 发送 WebSocket 消息的缓冲区最大大小 为 1MB（1024 * 1024 字节），如果消息超过这个大小，连接将会被关闭
                .setSendTimeLimit(30000);// 设置 发送 WebSocket 消息的最大时间限制 为 30 秒（30000 毫秒），如果发送消息的时间超过这个限制，连接可能会被关闭
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