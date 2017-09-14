/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
 package com.foilen.infra.ui;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.foilen.infra.ui.InfraUiDbLiveSpringConfig.ConfigUiDbLiveConfigLocal;
import com.foilen.infra.ui.InfraUiDbLiveSpringConfig.ConfigUiDbLiveConfigTestProd;

@Configuration
@Import({ ConfigUiDbLiveConfigLocal.class, ConfigUiDbLiveConfigTestProd.class })
@EnableTransactionManagement
public class InfraUiDbLiveSpringConfig {

    @Configuration
    @Profile({ "JUNIT", "LOCAL" })
    public static class ConfigUiDbLiveConfigLocal {
        // Uses Spring Boot datasource
    }

    @Configuration
    @Profile({ "TEST", "PROD" })
    @PropertySource({ "classpath:/com/foilen/infra/ui/application-common.properties", "classpath:/com/foilen/infra/ui/application-${MODE}.properties" })
    public static class ConfigUiDbLiveConfigTestProd {
        // Uses Spring Boot datasource
    }

}
