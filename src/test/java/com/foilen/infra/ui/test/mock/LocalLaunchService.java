/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.test.mock;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;

public class LocalLaunchService {

    private FakeDataService fakeDataService;

    public LocalLaunchService(FakeDataService fakeDataService) {
        this.fakeDataService = fakeDataService;
    }

    @Order(3)
    @EventListener
    public void createTheData(ContextRefreshedEvent event) {
        fakeDataService.createAll();
    }

}
