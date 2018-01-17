/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.test;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.foilen.login.spring.services.FoilenLoginService;

@Configuration
public class ConfigUiTestConfig {

    @Bean
    public FoilenLoginService foilenLoginServiceMock() {
        return new FoilenLoginServiceMock();
    }

}
