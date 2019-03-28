/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.plugin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.foilen.infra.plugin.v1.core.service.MessagingService;
import com.foilen.infra.ui.services.AlertManagementService;
import com.foilen.smalltools.tools.AbstractBasics;

@Component
public class MessagingServiceImpl extends AbstractBasics implements MessagingService {

    @Autowired
    private AlertManagementService alertManagementService;

    @Override
    public void alertingError(String shortDescription, String longDescription) {
        alertManagementService.queueAlert("[ERROR] " + shortDescription, longDescription);
    }

    @Override
    public void alertingInfo(String shortDescription, String longDescription) {
        alertManagementService.queueAlert("[INFO] " + shortDescription, longDescription);
    }

    @Override
    public void alertingWarn(String shortDescription, String longDescription) {
        alertManagementService.queueAlert("[WARN] " + shortDescription, longDescription);
    }

}
