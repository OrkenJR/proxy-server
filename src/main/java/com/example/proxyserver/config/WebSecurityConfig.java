package com.example.proxyserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class WebSecurityConfig {
    private static final String FRONTEND_LOCALHOST = "http://localhost:4200";

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().antMatchers("/login", "/register");
    }

    @Bean
    CorsConfigurationSource corsConfiguration() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.applyPermitDefaultValues();
        corsConfig.setAllowedMethods(List.of("POST", "GET", "PUT", "DELETE"));
        corsConfig.setAllowedOrigins(List.of(FRONTEND_LOCALHOST));
        corsConfig.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return source;
    }

    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http) throws Exception {
        http.csrf()
                .disable()
                .authorizeExchange()
                .pathMatchers("/login", "/register")
                .permitAll()
                .and()
                .authorizeExchange()
                .anyExchange()
                .permitAll()
                .and()
                .httpBasic()
                .and()
                .authenticationManager(new ReactiveAuthenticationManager() {
                    @Override
                    public Mono<Authentication> authenticate(Authentication authentication) {
                        return null;
                    }
                });
        return http.build();
    }

}
