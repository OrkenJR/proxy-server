package com.example.proxyserver;

import com.example.proxyserver.feign.AuthFeign;
import com.example.proxyserver.model.ErrorResponseDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.function.Predicate;
import java.util.stream.Stream;

@RefreshScope
@Component
@Slf4j
@RequiredArgsConstructor
public class AuthPostFilter implements GatewayFilter {
    private final AuthFeign authFeign;
    public Predicate<ServerHttpRequest> isSecured = request -> Stream.of("/login", "/register").noneMatch(uri -> request.getURI().getPath().contains(uri));

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest req = exchange.getRequest();
        log.info("URL is - " + req.getURI().getPath());
        if (isSecured.test(req)) {
            try {
                String bearerToken = req.getHeaders().getFirst("Authorization");
                log.info("Bearer Token: " + bearerToken);

                if (StringUtils.isBlank(bearerToken)) {
                    log.warn("Token is null");
                    return this.onError(exchange, "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
                }
                ResponseEntity<Boolean> response = authFeign.validateToken(bearerToken);
                if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                    HttpStatus errorCode = HttpStatus.BAD_GATEWAY;
                    String errorMsg = HttpStatus.BAD_GATEWAY.getReasonPhrase();
                    return onError(exchange, errorMsg, errorCode);
                } else if (response.getBody() != null && Boolean.TRUE.equals(response.getBody())) {
                    return chain.filter(exchange);
                }
            } catch (ExpiredJwtException e) {
                log.warn("Token Expired!");
                return this.onError(exchange, "Authorization header has expired", HttpStatus.UNAUTHORIZED);
            } catch (JwtException e) {
                log.warn("Token Invalid!");
                return this.onError(exchange, "Authorization header is invalid", HttpStatus.UNAUTHORIZED);
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
            byte[] byteData = objMapper.writeValueAsBytes(new ErrorResponseDto(httpStatus, "auth-error", err, new Date()));
            return response.writeWith(Mono.just(byteData).map(t -> dataBufferFactory.wrap(t)));

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return response.setComplete();
    }
}
