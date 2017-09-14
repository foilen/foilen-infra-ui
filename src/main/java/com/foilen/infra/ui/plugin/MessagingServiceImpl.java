/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
 package com.foilen.infra.ui.plugin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.foilen.infra.plugin.v1.core.service.MessagingService;
import com.foilen.services.AlertNotificationService;
import com.foilen.smalltools.tools.AbstractBasics;

@Component
public class MessagingServiceImpl extends AbstractBasics implements MessagingService {

    @Autowired
    private AlertNotificationService alertNotificationService;

    @Override
    public void alertingError(String shortDescription, String longDescription) {
        alertNotificationService.sendAlert("[ERROR] " + shortDescription, longDescription);
    }

    @Override
    public void alertingInfo(String shortDescription, String longDescription) {
        alertNotificationService.sendAlert("[INFO] " + shortDescription, longDescription);
    }

    @Override
    public void alertingWarn(String shortDescription, String longDescription) {
        alertNotificationService.sendAlert("[WARN] " + shortDescription, longDescription);
    }

}
