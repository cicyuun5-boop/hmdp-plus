package org.javaup.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 拒绝外部访问内部接口路径。
 * <p>
 * 约定：内部接口路径统一带 /inner 段（如 /user/inner/**），仅供服务间直连调用，
 * 不允许从网关进入，避免内部接口暴露到公网入口。
 */
@Component
public class InternalApiBlockFilter implements GlobalFilter, Ordered {

    private static final String INNER_PATH_PATTERN = ".*/inner(/.*)?";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (path.matches(INNER_PATH_PATTERN)) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
