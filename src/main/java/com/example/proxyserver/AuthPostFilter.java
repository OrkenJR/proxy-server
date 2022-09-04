package com.example.proxyserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
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

import java.util.function.Predicate;
import java.util.stream.Stream;

@RefreshScope
@Component
@Slf4j
public class AuthPostFilter implements GatewayFilter {
    public Predicate<ServerHttpRequest> isSecured = request -> Stream.of("/login", "/register").noneMatch(uri -> request.getURI().getPath().contains(uri));

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest req = exchange.getRequest();
        log.info("**************************************************************************");
        log.info("URL is - " + req.getURI().getPath());
        if (isSecured.test(req)) {
            try {
                boolean hasAccess = authenticate(req);
                if (!hasAccess) {
                    log.info("Token Ivalid!");
                    log.info("**************************************************************************");
                    return this.onError(exchange, "Authorization header is invalid", HttpStatus.UNAUTHORIZED);
                }
            } catch (ExpiredJwtException e) {
                log.info("Token Expired!");
                log.info("**************************************************************************");
                return this.onError(exchange, "Authorization header has expired", HttpStatus.UNAUTHORIZED);
            } catch (JwtException e) {
                log.info("Token Ivalid!");
                log.info("**************************************************************************");
                return this.onError(exchange, "Authorization header is invalid", HttpStatus.UNAUTHORIZED);
            }
        }
        log.info("**************************************************************************");
        return chain.filter(exchange);
    }

    //TODO
    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        DataBufferFactory dataBufferFactory = exchange.getResponse().bufferFactory();
        ObjectMapper objMapper = new ObjectMapper();
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        try {
            response.getHeaders().add("Content-Type", "application/json");
            byte[] byteData = objMapper.writeValueAsBytes("error");
            return response.writeWith(Mono.just(byteData).map(t -> dataBufferFactory.wrap(t)));

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return response.setComplete();
    }


    //TODO validate using authFeign
    private boolean authenticate(ServerHttpRequest request) {
        String token = request.getHeaders().getFirst("Authorization");
        return false;
    }
}
