/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.mvc.ui;

import org.springframework.web.servlet.ModelAndView;

public interface UiErrorRedirectHandler {

    void execute(UiSuccessErrorView uiSuccessErrorView, ModelAndView modelAndView);

}
