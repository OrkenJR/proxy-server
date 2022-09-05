package com.example.proxyserver.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.Date;

@Data
@AllArgsConstructor
@Builder
public class ErrorResponseDto {


    private HttpStatus status;
    private String error;
    private String errorMsg;
    private Date date;

}
