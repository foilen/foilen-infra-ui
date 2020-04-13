/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.web.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiSecurityConfig {

    @Bean
    public ApiWebSecurityRepositoryConfigurer apiWebSecurityRepositoryConfigurer() {
        return new ApiWebSecurityRepositoryConfigurer();
    }

}
