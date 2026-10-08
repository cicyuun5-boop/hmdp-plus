package org.javaup.api;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.javaup.constant.Constant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 给所有 Feign 请求自动带上服务间内部调用凭据。
 * <p>
 * 放在 hmdp-api 中，任何依赖本模块的服务都会自动装配，无需各自重复实现。
 */
@Component
public class FeignInternalAuthInterceptor implements RequestInterceptor {

    private final String internalToken;

    public FeignInternalAuthInterceptor(@Value("${hmdp.internal.token}") String internalToken) {
        this.internalToken = internalToken;
    }

    @Override
    public void apply(RequestTemplate template) {
        template.header(Constant.INTERNAL_TOKEN_HEADER, internalToken);
    }
}
