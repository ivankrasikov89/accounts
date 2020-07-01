package com.seven.tech.accounts.configuration;

import com.seven.tech.accounts.BaseResult;
import com.seven.tech.accounts.exception.BaseAccountException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class BaseAccountExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = BaseAccountException.class)
    public ResponseEntity<Object> handleConflict(RuntimeException ex, WebRequest request) {
        BaseResult result = new BaseResult<>(((BaseAccountException) ex).getCode(), ex.getMessage());
        return handleExceptionInternal(ex, result, new HttpHeaders(), HttpStatus.OK, request);
    }

}