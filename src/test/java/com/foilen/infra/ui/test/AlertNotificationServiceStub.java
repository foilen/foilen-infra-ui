/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.test;

import com.foilen.services.AlertNotificationService;

public class AlertNotificationServiceStub implements AlertNotificationService {

    @Override
    public void sendAlert(String subject, String content) {
        throw new RuntimeException("Not stubbed");
    }

}
