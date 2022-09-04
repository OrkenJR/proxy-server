package com.example.proxyserver.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "auth-service", path = "/auth")
@Service
public interface AuthFeign {

    @GetMapping(value = "/validateToken")
    ResponseEntity<Boolean> validateToken(@RequestBody String token);

}
