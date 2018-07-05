/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.web.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class ApiSecurityConfig {

    @Profile({ "JUNIT", "LOCAL" })
    @Bean
    public ApiWebSecurityH2Configurer apiWebSecurityH2Configurer() {
        return new ApiWebSecurityH2Configurer();
    }

    @Profile({ "TEST", "PROD" })
    @Bean
    public ApiWebSecurityMysqlConfigurer apiWebSecurityMysqlConfigurer() {
        return new ApiWebSecurityMysqlConfigurer();
    }

}
