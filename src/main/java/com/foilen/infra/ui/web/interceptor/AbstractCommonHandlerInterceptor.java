/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
 package com.foilen.infra.ui.web.interceptor;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class AbstractCommonHandlerInterceptor extends HandlerInterceptorAdapter {

    protected boolean isRedirect(ModelAndView modelAndView) {
        return modelAndView.getViewName().startsWith("redirect:");
    }

}
