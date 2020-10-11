package com.laoyang.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.server.RequestPath;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

/**
 * @author yyy
 * @Date 2020-07-24 14:33
 * @Email yangyouyuhd@163.com
 */
@Slf4j
@Component
public class HelloFilter implements GlobalFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        RequestPath path = exchange.getRequest().getPath();
        InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();

        log.info("访问路径："+path.toString());
        log.info("来源host地址"+remoteAddress.getHostString());

        return chain.filter(exchange);
    }
}
