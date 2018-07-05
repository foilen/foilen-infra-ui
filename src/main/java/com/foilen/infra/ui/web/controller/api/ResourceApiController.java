/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

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

import com.foilen.infra.api.request.ChangesRequest;
import com.foilen.infra.api.response.ResponseWithStatus;
import com.foilen.infra.ui.services.ApiResourceManagementService;

@RequestMapping(value = "api/resource", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
@RestController
public class ResourceApiController {

    @Autowired
    private ApiResourceManagementService apiResourceManagementService;

    @PostMapping("applyChanges")
    public ResponseWithStatus applyChanges(Authentication authentication, @RequestBody ChangesRequest changesRequest) {
        return apiResourceManagementService.applyChanges(authentication.getName(), changesRequest);
    }

}
