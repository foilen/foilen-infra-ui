/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.wiring.BeanConfigurerSupport;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.web.csrf.CsrfTokenRepository;

import com.foilen.infra.ui.localonly.FakeDataService;
import com.foilen.infra.ui.localonly.FakeDataServiceImpl;
import com.foilen.infra.ui.localonly.LocalLaunchService;
import com.foilen.infra.ui.visual.MenuEntry;
import com.foilen.smalltools.spring.security.CookiesGeneratedCsrfTokenRepository;
import com.foilen.smalltools.tools.AssertTools;
import com.foilen.smalltools.tools.CharsetTools;

@Configuration
@ComponentScan({ "com.foilen.infra.ui.config", //
        "com.foilen.infra.ui.db.dao", //
        "com.foilen.infra.ui.plugin", //
        "com.foilen.infra.ui.services", //
        "com.foilen.infra.ui.tasks", //
        "com.foilen.infra.ui.visual" })
@EnableAutoConfiguration
@EnableScheduling
@PropertySource({ "classpath:/com/foilen/infra/ui/application-common.properties", "classpath:/com/foilen/infra/ui/application-${MODE}.properties" })
public class InfraUiSpringConfig {

    @Configuration
    @Profile({ "JUNIT", "LOCAL" })
    public static class ConfigUiConfigLocal {
        @Bean
        public FakeDataService fakeDataService() {
            return new FakeDataServiceImpl();
        }

        @Bean
        public LocalLaunchService localLaunchService() {
            return new LocalLaunchService(fakeDataService());
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

    @Configuration
    @Profile({ "TEST", "PROD" })
    public static class ConfigUiConfigProd {
        @Bean
        public MessageSource messageSource() {
            ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
            messageSource.setBasename("classpath:/WEB-INF/infra/ui/messages/messages");
            messageSource.setDefaultEncoding(CharsetTools.UTF_8.name());
            messageSource.setUseCodeAsDefaultMessage(true);
            return messageSource;
        }
    }

    @Bean
    public BeanConfigurerSupport beanConfigurerSupport() {
        return new BeanConfigurerSupport();
    }

    @Bean
    public ConversionService conversionService() {
        return new DefaultConversionService();
    }

    @Bean
    public CsrfTokenRepository csrfTokenRepository(@Value("${infraUi.csrfSalt}") String csrfSalt) {
        AssertTools.assertNotNull(csrfSalt);
        return new CookiesGeneratedCsrfTokenRepository().setSalt(csrfSalt).addCookieNames("fl_user_id", "fl_date", "fl_signature");
    }

    @Bean
    public MenuEntry rootMenuEntry() {
        MenuEntry menuEntry = new MenuEntry();

        // Infrastructure
        MenuEntry child = menuEntry.addChild("infrastructure");
        child.addChild("machineGraphics").setUri("/machineGraphics/list").addUriStartsWith("/machineGraphics/");
        child.addChild("machineBootstrap").setUri("/machineBootstrap/list").addUriStartsWith("/machineBootstrap/");

        // Plugins
        menuEntry.addChild("plugin").setUri("/plugin/list").addUriStartsWith("/plugin/");
        menuEntry.addChild("pluginresources").setUri("/pluginresources/list").addUriStartsWith("/pluginresources/");

        // ApiUser
        menuEntry.addChild("apiUser").setUri("/apiUser/list").addUriStartsWith("/apiUser/");

        return menuEntry;
    }

}
