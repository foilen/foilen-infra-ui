/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.localonly;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
public class LocalLaunchService {

    @Autowired
    private FakeDataService fakeDataService;
    @Autowired
    private FoilenLoginSecurityFakeUserConfig foilenLoginSecurityFakeUserConfig;

    @Order(3)
    @EventListener
    public void createTheData(ContextRefreshedEvent event) {
        fakeDataService.clearAll();
        fakeDataService.createAll();
        foilenLoginSecurityFakeUserConfig.init();
    }

}
