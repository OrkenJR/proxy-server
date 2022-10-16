package com.example.proxyserver.filters;

import com.example.proxyserver.model.ErrorResponseDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.iitu.cfaslib.feign.AuthFeign;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static kz.iitu.cfaslib.util.CfasConstants.AUTHORIZATION_HEADER;
import static kz.iitu.cfaslib.util.SecureUtil.stripToken;

@RefreshScope
@Component
@RequiredArgsConstructor
public class AuthPostFilter implements GatewayFilter {

    private final AuthFeign authFeign;

    private static final Predicate<ServerHttpRequest> isSecured =
            request -> Stream.of("/login", "/register").noneMatch(uri -> request.getURI().getPath().contains(uri));

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest req = exchange.getRequest();

        if (isSecured.test(req)) {
            try {
                String token = stripToken(req.getHeaders().getFirst(AUTHORIZATION_HEADER));
                if (StringUtils.isBlank(token)) {
                    return this.onError(exchange, "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
                }

                if (!validateToken(token)) {
                    return this.onError(exchange, "Authorization header is invalid", HttpStatus.UNAUTHORIZED);
                }

            } catch (RuntimeException e) {
                return onError(exchange, e.getMessage(), HttpStatus.UNAUTHORIZED);
            }
        }
        return chain.filter(exchange);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        DataBufferFactory dataBufferFactory = exchange.getResponse().bufferFactory();
        ObjectMapper objMapper = new ObjectMapper();
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        try {
            response.getHeaders().add("Content-Type", "application/json");
            // todo era Inappropriate blocking method call
            byte[] byteData = objMapper.writeValueAsBytes(new ErrorResponseDto(httpStatus, "auth-error", err, new Date()));
            return response.writeWith(Mono.just(byteData).map(dataBufferFactory::wrap));

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return response.setComplete();
    }

    @SneakyThrows
    public boolean validateToken(String token) {
        CompletableFuture<Boolean> completableFuture = CompletableFuture.supplyAsync(() -> authFeign.validateToken(token));

        Boolean resp;
        try {
            resp = completableFuture.get();
        } catch (Exception ex) {
            return false;
        }

        return resp;
    }

}
