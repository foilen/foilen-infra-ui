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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foilen.infra.apitmp.model.ApplicationDetailsResult;
import com.foilen.infra.ui.services.ApplicationService;

@RequestMapping(value = "api/app", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
@RestController
@SwaggerExpose
public class AppApiController {

    @Autowired
    private ApplicationService applicationService;

    @GetMapping("details")
    public ApplicationDetailsResult details(Authentication authentication) {
        return applicationService.getDetails(authentication.getName());
    }

}
