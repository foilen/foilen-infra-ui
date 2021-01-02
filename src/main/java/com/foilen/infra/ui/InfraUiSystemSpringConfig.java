/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.foilen.infra.plugin.core.system.common.context.CommonServicesContextBean;
import com.foilen.infra.plugin.core.system.common.context.InternalServicesContextBean;
import com.foilen.infra.plugin.core.system.common.service.IPPluginServiceImpl;
import com.foilen.infra.plugin.core.system.common.service.TimerServiceInExecutorImpl;
import com.foilen.infra.plugin.core.system.common.service.TranslationServiceImpl;
import com.foilen.infra.plugin.core.system.mongodb.service.MessagingServiceMongoDbImpl;
import com.foilen.infra.plugin.core.system.mongodb.service.ResourceServicesInMongoDbImpl;
import com.foilen.infra.plugin.core.system.mongodb.spring.ResourceServicesMongoDBSpringConfig;
import com.foilen.infra.plugin.v1.core.common.InfraPluginCommonInit;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.context.internal.InternalServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.changes.ChangeExecutionHook;
import com.foilen.infra.plugin.v1.core.service.IPPluginService;
import com.foilen.infra.plugin.v1.core.service.MessagingService;
import com.foilen.infra.plugin.v1.core.service.TimerService;
import com.foilen.infra.plugin.v1.core.service.TranslationService;
import com.foilen.infra.plugin.v1.core.service.internal.InternalChangeService;
import com.foilen.infra.ui.services.AuditingService;
import com.foilen.infra.ui.services.ReportService;
import com.foilen.infra.ui.services.hook.AuditingChangeExecutionHook;
import com.foilen.infra.ui.services.hook.ReportingChangeExecutionHook;
import com.foilen.infra.ui.services.hook.UserDetailsChangeExecutionHook;
import com.foilen.infra.ui.services.hook.UserPermissionChangeExecutionHook;

/**
 * The system configuration. Mostly copied from {@link ResourceServicesMongoDBSpringConfig}.
 */
@Configuration
@ComponentScan("com.foilen.infra.plugin.core.system.mongodb.service")
public class InfraUiSystemSpringConfig {

    @Autowired
    private AuditingService auditingService;
    @Autowired
    private CommonServicesContext commonServicesContext;
    @Autowired
    private InternalServicesContext internalServicesContext;
    @Autowired
    private ReportService reportService;

    @Value("${infraUi.infiniteLoopTimeoutInMs}")
    private long infiniteLoopTimeoutInMs;

    @Bean
    public CommonServicesContext commonServicesContext() {
        return new CommonServicesContextBean();
    }

    @PostConstruct
    public void init() {
        InternalChangeService internalChangeService = internalServicesContext.getInternalChangeService();
        internalChangeService.setInfiniteLoopTimeoutInMs(infiniteLoopTimeoutInMs);
        internalChangeService.getDefaultChangeExecutionHooks().add(userPermissionChangeExecutionHook());
        internalChangeService.getDefaultChangeExecutionHooks().add(new AuditingChangeExecutionHook(auditingService));
        internalChangeService.getDefaultChangeExecutionHooks().add(new ReportingChangeExecutionHook(reportService));
        internalChangeService.getDefaultChangeExecutionHooks().add(userDetailsChangeExecutionHook());
        InfraPluginCommonInit.init(commonServicesContext, internalServicesContext);
    }

    @Bean
    public InternalServicesContext internalServicesContext() {
        return new InternalServicesContextBean();
    }

    @Bean
    public IPPluginService ipPluginService() {
        return new IPPluginServiceImpl();
    }

    @Bean
    public MessagingService messagingService() {
        return new MessagingServiceMongoDbImpl();
    }

    @Bean
    public ResourceServicesInMongoDbImpl resourceServices() {
        ResourceServicesInMongoDbImpl resourceServicesInMongoDb = new ResourceServicesInMongoDbImpl();
        return resourceServicesInMongoDb;
    }

    @Bean
    public TimerService timerService() {
        return new TimerServiceInExecutorImpl();
    }

    @Bean
    public TranslationService translationService() {
        return new TranslationServiceImpl();
    }

    @Bean
    public ChangeExecutionHook userDetailsChangeExecutionHook() {
        return new UserDetailsChangeExecutionHook();
    }

    @Bean
    public UserPermissionChangeExecutionHook userPermissionChangeExecutionHook() {
        return new UserPermissionChangeExecutionHook();
    }

}
