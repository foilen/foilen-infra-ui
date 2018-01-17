/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.plugin;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.foilen.infra.plugin.core.system.junits.AbstractIPResourceServiceTest;
import com.foilen.infra.plugin.v1.core.base.resources.helper.UnixUserAvailableIdHelper;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.context.internal.InternalServicesContext;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.ui.InfraUiSpringConfig;
import com.foilen.infra.ui.localonly.FakeDataService;
import com.foilen.infra.ui.test.AlertNotificationServiceStub;
import com.foilen.infra.ui.test.ConfigUiTestConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ConfigUiTestConfig.class, InfraUiSpringConfig.class, AlertNotificationServiceStub.class })
@ActiveProfiles("JUNIT")
public class PluginSystemServicesImplTest extends AbstractIPResourceServiceTest {

    @BeforeClass
    public static void updateProperties() {
        System.setProperty("MODE", "JUNIT");
        System.setProperty("infraUi.csrfSalt", "aaaaaa");
        System.setProperty("infraUi.baseUrl", "http://ui.example.com");
    }

    @Autowired
    private CommonServicesContext commonServicesContext;
    @Autowired
    private InternalServicesContext internalServicesContext;
    @Autowired
    private FakeDataService fakeDataService;

    @Autowired
    private IPResourceService ipResourceService;

    @Override
    @Before
    public void createFakeData() {
        fakeDataService.clearAll();
        UnixUserAvailableIdHelper.init(ipResourceService);
        super.createFakeData();
    }

    @Override
    protected CommonServicesContext getCommonServicesContext() {
        return commonServicesContext;
    }

    @Override
    protected InternalServicesContext getInternalServicesContext() {
        return internalServicesContext;
    }

}
