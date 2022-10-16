package com.example.proxyserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class WebSecurityConfig {

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().antMatchers("/login", "/register");
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
                })
                .cors();
        return http.build();
    }

}
