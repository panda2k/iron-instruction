package com.ironinstruction.api.utils;

import com.ironinstruction.api.errors.ErrorResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalErrorHandler extends ResponseEntityExceptionHandler {
    @Override
    public ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException e, HttpHeaders headers, HttpStatus status, WebRequest req) {
        return new ResponseEntity<Object>(new ErrorResponse("Invalid body (general)") ,HttpStatus.BAD_REQUEST);
    }
}
