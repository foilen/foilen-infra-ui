/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.test;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import com.foilen.infra.resource.infraconfig.model.InfraUiConfig;
import com.foilen.infra.ui.test.mock.EmailServiceMock;
import com.foilen.infra.ui.test.mock.FakeDataService;
import com.foilen.infra.ui.test.mock.FakeDataServiceImpl;
import com.foilen.login.spring.services.FoilenLoginService;
import com.foilen.smalltools.tools.CharsetTools;

@Configuration
@ComponentScan({ "com.foilen.infra.ui.upgrades.mongodb" })
public class ConfigUiTestConfig {

    @Primary
    @Bean
    public EmailServiceMock emailServiceMock() {
        return new EmailServiceMock();
    }

    @Bean
    public FakeDataService fakeDataService() {
        return new FakeDataServiceImpl();
    }

    @Bean
    public FoilenLoginService foilenLoginServiceMock() {
        return new FoilenLoginServiceMock();
    }

    @Bean
    public InfraUiConfig infraUiConfig() {
        return new InfraUiConfig();
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:/WEB-INF/infra/ui/messages/messages");
        messageSource.setDefaultEncoding(CharsetTools.UTF_8.name());
        messageSource.setUseCodeAsDefaultMessage(true);
        return messageSource;
    }

}
