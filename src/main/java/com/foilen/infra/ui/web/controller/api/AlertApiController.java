/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.web.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foilen.infra.api.request.RequestAlert;
import com.foilen.infra.ui.services.AlertManagementService;
import com.foilen.smalltools.restapi.model.FormResult;
import com.foilen.smalltools.tools.AbstractBasics;

@RequestMapping(value = "api/alert", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
@RestController
@SwaggerExpose
public class AlertApiController extends AbstractBasics {

    @Autowired
    private AlertManagementService alertManagementService;

    @PostMapping()
    public FormResult sendAlert(Authentication authentication, @RequestBody RequestAlert requestAlert) {
        return alertManagementService.queueAlert(authentication.getName(), requestAlert.getSubject(), requestAlert.getContent());
    }

    @PostMapping("/")
    public FormResult sendAlertSlash(Authentication authentication, @RequestBody RequestAlert requestAlert) {
        return alertManagementService.queueAlert(authentication.getName(), requestAlert.getSubject(), requestAlert.getContent());
    }

}
