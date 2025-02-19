package com.imserver.resolver;

import com.imserver.annotation.WsSessionId;
import com.imserver.context.UserSessionContext;
import com.imserver.util.AuthUtil;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class WsSessionIdArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(WsSessionId.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        String ide = webRequest.getHeader(AuthUtil.HEADER_NAME_TOKEN);
        return UserSessionContext.getWsSessionByToken(ide);
    }

}