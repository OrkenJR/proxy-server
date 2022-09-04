package com.example.proxyserver;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RouteConfig {
    private final AuthPostFilter authPostFilter;

    @Bean
    public RouteLocator routes(
            RouteLocatorBuilder builder) {
        return builder.routes()

                .route("auth-route", r -> r.path("/auth/**")
                        .filters(f -> f
                                .filter(authPostFilter)
                                .stripPrefix(1))
                        .uri("lb://auth"))
                .route("user-management-route", r -> r.path()
                        .filters(f -> f
                                .filter(authPostFilter)
                                .stripPrefix(1))
                        .uri("lb://user-management"))
                .build();
    }
}
