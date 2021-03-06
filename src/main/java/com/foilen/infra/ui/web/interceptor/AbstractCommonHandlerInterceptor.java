/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.web.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public abstract class AbstractCommonHandlerInterceptor implements HandlerInterceptor {

    protected boolean isRedirect(ModelAndView modelAndView) {
        return modelAndView.getViewName().startsWith("redirect:");
    }

}
