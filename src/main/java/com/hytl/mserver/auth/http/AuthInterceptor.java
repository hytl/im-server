package com.hytl.mserver.auth.http;

import com.hytl.mserver.context.UserSessionContext;
import com.hytl.mserver.model.UserInfo;
import com.hytl.mserver.util.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 鉴权逻辑
        String token = request.getHeader(AuthUtil.HEADER_NAME_TOKEN);
        if (token != null) {
            UserInfo userInfo = UserSessionContext.getUserInfoByToken(token);
            if (userInfo == null) {
                // TODO TOKEN校验
                UserInfo userInfoByToken = null;
                try {
                    userInfoByToken = null;
                    if (userInfoByToken != null) {
                        UserSessionContext.online(token, userInfoByToken.userId(), userInfoByToken.username());
                        return true; // 鉴权通过
                    }
                } catch (Exception e) {
                    log.error("认证失败：fail to get token info", e);
                }
            } else {
                return true;
            }
        }

        // 鉴权失败
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("Unauthorized");
        return false;
    }
}