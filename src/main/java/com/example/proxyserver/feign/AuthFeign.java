package com.example.proxyserver.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "auth-service", path = "/auth")
@Service
public interface AuthFeign {

    @PostMapping(value = "/validateToken")
    boolean validateToken(@RequestParam String token);

}
