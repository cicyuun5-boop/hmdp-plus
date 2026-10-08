package org.javaup.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.javaup.constant.Constant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 内部接口鉴权拦截器。
 * <p>
 * /user/inner/** 是供其他微服务调用的接口，不走用户态登录（调用方没有用户 token），
 * 改为校验服务间共享凭据，防止外部绕过网关直接访问内部接口。
 */
@Component
public class InternalApiAuthInterceptor implements HandlerInterceptor {

    private final String internalToken;

    public InternalApiAuthInterceptor(@Value("${hmdp.internal.token}") String internalToken) {
        this.internalToken = internalToken;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader(Constant.INTERNAL_TOKEN_HEADER);
        if (token == null || !token.equals(internalToken)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }
}
