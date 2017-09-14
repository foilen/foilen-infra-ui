/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
 package com.foilen.infra.ui.plugin;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.stereotype.Component;

import com.foilen.infra.plugin.v1.core.service.SecurityService;

@Component
public class PluginSecurityService implements SecurityService {

    @Autowired
    private CsrfTokenRepository csrfTokenRepository;

    @Override
    public String getCsrfParameterName() {
        return "_csrf";
    }

    @Override
    public String getCsrfValue(Object request) {
        return csrfTokenRepository.generateToken((HttpServletRequest) request).getToken();
    }

}
