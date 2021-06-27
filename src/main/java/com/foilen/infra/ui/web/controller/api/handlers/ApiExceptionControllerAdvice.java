/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.web.controller.api.handlers;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.foilen.mvc.ui.UiException;
import com.foilen.smalltools.restapi.model.ApiError;
import com.foilen.smalltools.tools.AbstractBasics;

@ControllerAdvice
public class ApiExceptionControllerAdvice extends AbstractBasics {

    @Autowired
    private MessageSource messageSource;

    @ExceptionHandler
    protected ResponseEntity<Map<String, Object>> handleConflict(UiException e, Locale locale) {
        Map<String, Object> response = new HashMap<>();
        ApiError error = new ApiError(messageSource.getMessage(e.getMessage(), null, locale));
        response.put("error", error);
        logger.info("Got a UI Exception : {}", error);
        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    }

}
