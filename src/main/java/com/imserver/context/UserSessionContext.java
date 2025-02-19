package com.imserver.context;

import com.imserver.model.UserInfo;
import com.imserver.model.WsUserSessionMapping;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户会话上下文
 */
public final class UserSessionContext {
    // KEY:sessionId WebSocket会话映射
    private static final Map<String, WsUserSessionMapping> WS_USER_SESSION_MAMPPING_MAP = new ConcurrentHashMap<>();
    // KEY:userId
    private static final Map<String, UserInfo> USER_INFO_MAP = new ConcurrentHashMap<>();
    // KEY:token 用来判断用户是否认证
    private static final Map<String, UserInfo> TOKEN_USER_INFO_MAP = new ConcurrentHashMap<>();
    // KEY:WebSocket-sessionId VALUE:token
    private static final Map<String, String> WS_SESSIONID_TOKEN_MAP = new ConcurrentHashMap<>();
    // KEY:token VALUE:WebSocket-sessionId
    private static final Map<String, String> TOKEN_WS_SESSIONID_MAP = new ConcurrentHashMap<>();
    // KEY:userId VALUE:tokens
    public static final Map<String, Set<String>> USER_TOKENS_MAP = new ConcurrentHashMap<>();

    public static void wsSessionConnect(String userId, String wsSessionId) {
        WS_USER_SESSION_MAMPPING_MAP.put(wsSessionId, new WsUserSessionMapping(userId, wsSessionId));
    }

    public static void wsSessionDisconnect(String wsSessionId) {
        WS_USER_SESSION_MAMPPING_MAP.remove(wsSessionId);
    }

    public static WsUserSessionMapping getWsUserSessionInfo(String wsSessionId) {
        return WS_USER_SESSION_MAMPPING_MAP.get(wsSessionId);
    }

    public static synchronized void putWsSessionIdToken(String wsSessionId, String TOKEN) {
        WS_SESSIONID_TOKEN_MAP.put(wsSessionId, TOKEN);
        TOKEN_WS_SESSIONID_MAP.put(TOKEN, wsSessionId);
    }

    public static String getWsSessionByToken(String TOKEN) {
        return TOKEN_WS_SESSIONID_MAP.get(TOKEN);
    }

    public static synchronized void online(String TOKEN, String userId, String username) {
        UserInfo userInfo = new UserInfo(userId, username);
        USER_INFO_MAP.put(userId, userInfo);
        TOKEN_USER_INFO_MAP.put(TOKEN, userInfo);
        USER_TOKENS_MAP.computeIfAbsent(userId, key -> new HashSet<>()).add(TOKEN);
    }

    public static synchronized void offline(String userId, String wsSessionId) {
        String token = WS_SESSIONID_TOKEN_MAP.remove(wsSessionId);

        WS_USER_SESSION_MAMPPING_MAP.remove(wsSessionId);

        if (token != null) {
            TOKEN_WS_SESSIONID_MAP.remove(token);
            Set<String> TOKENs = USER_TOKENS_MAP.get(userId);
            if (!CollectionUtils.isEmpty(TOKENs)) {
                TOKENs.remove(token);
                // 如果用户所有客户端都离线，将用户最后需要清理的数据全部清除
                if (TOKENs.isEmpty()) {
                    USER_TOKENS_MAP.remove(userId);
                    USER_INFO_MAP.remove(userId);
                }
            }
        }
    }

    public static UserInfo getUserInfoByUserId(String userId) {
        return USER_INFO_MAP.get(userId);
    }

    public static UserInfo getUserInfoByToken(String token) {
        return TOKEN_USER_INFO_MAP.get(token);
    }
}