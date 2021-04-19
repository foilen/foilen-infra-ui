/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.wiring.BeanConfigurerSupport;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.web.csrf.CsrfTokenRepository;

import com.foilen.infra.ui.visual.MenuEntry;
import com.foilen.smalltools.spring.security.CookiesGeneratedCsrfTokenRepository;
import com.foilen.smalltools.tools.AssertTools;
import com.foilen.smalltools.tools.CharsetTools;

@Configuration
@ComponentScan({ //
        "com.foilen.infra.ui.converters", //
        "com.foilen.infra.ui.services", //
        "com.foilen.infra.ui.tasks", //
})
@EnableAutoConfiguration(exclude = { //
        DataSourceAutoConfiguration.class, //
        DataSourceTransactionManagerAutoConfiguration.class, //
        JpaRepositoriesAutoConfiguration.class, //
        MongoDataAutoConfiguration.class, //
})
@EnableScheduling
@PropertySource({ "classpath:/com/foilen/infra/ui/application-common.properties" })
public class InfraUiSpringConfig {

    @Configuration
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
    public ConversionService conversionService(List<Converter<?, ?>> converters) {
        DefaultConversionService conversionService = new DefaultConversionService();
        converters.forEach(c -> conversionService.addConverter(c));
        return conversionService;
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
        menuEntry.addChild("resources").setUri("/index.html#/resources").addUriStartsWith("/pluginresources/");

        // Users
        child = menuEntry.addChild("users");
        child.addChild("roles").setUri("/index.html#/roles").addUriStartsWith("/index.html#/roles");
        child.addChild("ownerRules").setUri("/index.html#/ownerRules").addUriStartsWith("/index.html#/ownerRules");
        child.addChild("userHumans").setUri("/index.html#/userHumans").addUriStartsWith("/index.html#/userHumans");
        child.addChild("userApis").setUri("/index.html#/userApis").addUriStartsWith("/index.html#/userApis");

        // Audits
        menuEntry.addChild("audits").setUri("/index.html#/audits").addUriStartsWith("/index.html#/audits");

        return menuEntry;
    }

}
