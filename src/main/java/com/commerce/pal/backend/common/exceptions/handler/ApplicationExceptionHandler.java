package com.commerce.pal.backend.common.exceptions.handler;

import com.commerce.pal.backend.common.exceptions.customExceptions.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

@RestControllerAdvice
@Slf4j
public class ApplicationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> errorMap = processFieldErrors(ex.getBindingResult());
        return ResponseEntity.ok(errorMap);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Map<String, String>> handleBindException(BindException ex) {
        Map<String, String> errorMap = processFieldErrors(ex.getBindingResult());
        return ResponseEntity.ok(errorMap);
    }

    @ExceptionHandler({BadRequestException.class,
            MultipartException.class,
            HttpMessageNotReadableException.class,
            HttpRequestMethodNotSupportedException.class,
            MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class,
            IllegalStateException.class
    })
    public ResponseEntity<ExceptionResponse> handleBadRequestException(Exception ex, HttpServletRequest request) {
        return buildResponse(ex.getMessage(), request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({UnauthorizedException.class, BannedUserException.class})
    public ResponseEntity<ExceptionResponse> handleUnauthorizedException(Exception ex, HttpServletRequest request) {
        return buildResponse(ex.getMessage(), request, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        return buildResponse(ex.getMessage(), request, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ExceptionResponse> handleResourceAlreadyExistsException(ResourceAlreadyExistsException ex, HttpServletRequest request) {
        return buildResponse(ex.getMessage(), request, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ExceptionResponse> handleForbiddenException(ForbiddenException ex, HttpServletRequest request) {
        return buildResponse(ex.getMessage(), request, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(Exception ex, HttpServletRequest request) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        String errorMessage = "Internal system error. Please try again or contact support.";
        log.error("INTERNAL_SERVER_ERROR: " + ex.getMessage(), ex);
        ex.printStackTrace();
        return buildResponse(errorMessage, request, httpStatus);
    }

    private ResponseEntity<ExceptionResponse> buildResponse(String errorMessage, HttpServletRequest request, HttpStatus httpStatus) {
        ExceptionResponse apiException = new ExceptionResponse();
        apiException.setTimeStamp(LocalDateTime.now().toString());
        apiException.setError(httpStatus);
        apiException.setStatusCode(String.valueOf(httpStatus.value()));
        apiException.setStatusMessage(errorMessage);
        apiException.setRequestPath(request.getRequestURI());

        return ResponseEntity.ok(apiException);
    }

    private Map<String, String> processFieldErrors(BindingResult bindingResult) {
        Map<String, String> errorMap = new HashMap<>();
        for (FieldError error : bindingResult.getFieldErrors()) {
            errorMap.put(error.getField(), error.getDefaultMessage());
        }
        return errorMap;
    }
}


