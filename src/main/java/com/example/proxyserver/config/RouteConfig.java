package com.example.proxyserver.config;

import com.example.proxyserver.filters.AuthPostFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties
@Component
@RequiredArgsConstructor
public class RouteConfig {
    private final AuthPostFilter authPostFilter;

    @Bean
    public RouteLocator routes(
            RouteLocatorBuilder builder) {
        return builder.routes()

                .route("auth-service", r -> r.path("/auth/**").filters(f -> f.filter(authPostFilter))
                        .uri("lb://auth-service"))

                .route("user-management-route", r -> r.path("/user-api/**").filters(f -> f.filter(authPostFilter))
                        .uri("lb://user-management"))

                .route("manufacture-service-route", r -> r.path("/manufacture-api/**").filters(f -> f.filter(authPostFilter))
                        .uri("lb://manufacture-service"))

                .build();
    }

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }
}
