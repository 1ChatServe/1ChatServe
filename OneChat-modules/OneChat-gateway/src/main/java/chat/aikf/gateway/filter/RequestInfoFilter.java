package chat.aikf.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RequestInfoFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpRequest.Builder builder = request.mutate();

        // 添加原始请求信息
        builder.header("X-Original-Client-IP", getClientIp(request));
        builder.header("X-Original-Method", request.getMethod().name());
        builder.header("X-Original-URI", request.getURI().toString());
        builder.header("X-Original-Path", request.getPath().value());

        // 传递认证信息
        String auth = request.getHeaders().getFirst("Authorization");
        if (auth != null) {
            builder.header("Authorization", auth);
        }

        // 传递用户代理
        String userAgent = request.getHeaders().getFirst("User-Agent");
        if (userAgent != null) {
            builder.header("User-Agent", userAgent);
        }

        return chain.filter(exchange.mutate().request(builder.build()).build());
    }

    private String getClientIp(ServerHttpRequest request) {
        // 1. 尝试从代理头部获取
        String ip = request.getHeaders().getFirst("X-Real-IP");
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeaders().getFirst("X-Forwarded-For");
        }

        // 2. 从远程地址获取
        if (ip == null || ip.isEmpty()) {
            if (request.getRemoteAddress() != null) {
                ip = request.getRemoteAddress().getAddress().getHostAddress();
            }
        }

        return ip != null ? ip : "unknown";
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
