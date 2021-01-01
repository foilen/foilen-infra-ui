/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.foilen.infra.ui.services.AlertManagementService;
import com.foilen.smalltools.tools.AbstractBasics;

@Component
public class AlertSendingTask extends AbstractBasics {

    @Autowired
    private AlertManagementService alertManagementService;

    // Every minute
    @Scheduled(fixedDelay = 60000L)
    public void sendAlerts() {
        logger.info("Starting sending Alerts");
        alertManagementService.sendQueuedAlerts();
        logger.info("Completed sending Alerts");
    }

}
