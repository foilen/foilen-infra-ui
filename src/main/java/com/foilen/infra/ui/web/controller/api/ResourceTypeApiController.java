/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.web.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foilen.infra.api.response.ResponseResourceTypesDetails;
import com.foilen.infra.ui.services.ApiResourceManagementService;

@RequestMapping(value = "api/resourceType", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
@RestController
public class ResourceTypeApiController {

    @Autowired
    private ApiResourceManagementService apiResourceManagementService;

    @GetMapping("")
    public ResponseResourceTypesDetails list() {
        return apiResourceManagementService.resourceTypeFindAll();
    }

}
