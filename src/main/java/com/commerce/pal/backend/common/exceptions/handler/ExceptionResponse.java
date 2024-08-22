package com.commerce.pal.backend.common.exceptions.handler;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;


@Getter
@Setter
public class ExceptionResponse {
    private String timeStamp;
    private HttpStatus error;
    private String statusCode;
    private String statusMessage;
    private String requestPath;
}
