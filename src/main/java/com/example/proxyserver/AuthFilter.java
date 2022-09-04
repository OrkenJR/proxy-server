package com.example.proxyserver;


import com.example.proxyserver.feign.AuthFeign;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.function.Predicate;
import java.util.stream.Stream;

//@Component
@Slf4j
@RequiredArgsConstructor
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {

    private final AuthFeign authFeign;
    private ObjectMapper objectMapper;
    public Predicate<ServerHttpRequest> isSecured = request -> Stream.of("/login", "/register").noneMatch(uri -> request.getURI().getPath().contains(uri));


    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            log.info("**************************************************************************");
            log.info("URL is - " + request.getURI().getPath());
            String bearerToken = request.getHeaders().getFirst("Authorization");
            log.info("Bearer Token: " + bearerToken);

            if (isSecured.test(request)) {

                ResponseEntity<Boolean> response = authFeign.validateToken(bearerToken);
                if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                    log.info("Error Happened");
                    HttpStatus errorCode = null;
                    String errorMsg = "";
                    errorCode = HttpStatus.BAD_GATEWAY;
                    errorMsg = HttpStatus.BAD_GATEWAY.getReasonPhrase();
                    return onError(exchange, String.valueOf(errorCode.value()), errorMsg, "JWT Authentication Failed", errorCode);
                }
            }

            return chain.filter(exchange);
        };
    }

    //TODO
    private Mono<Void> onError(ServerWebExchange exchange, String errCode, String err, String errDetails, HttpStatus httpStatus) {
        DataBufferFactory dataBufferFactory = exchange.getResponse().bufferFactory();
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        try {
            response.getHeaders().add("Content-Type", "application/json");
            byte[] byteData = objectMapper.writeValueAsBytes("error");
            return response.writeWith(Mono.just(byteData).map(t -> dataBufferFactory.wrap(t)));

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return response.setComplete();
    }

    @NoArgsConstructor
    public static class Config {


    }
}