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

import com.foilen.infra.apitmp.model.AuditItemWithPagination;
import com.foilen.infra.apitmp.request.RequestAuditItem;
import com.foilen.infra.ui.services.ApiAuditManagementService;

@RequestMapping(value = "api/audit", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
@RestController
@SwaggerExpose
public class AuditApiController {

    @Autowired
    private ApiAuditManagementService apiAuditManagementService;

    @GetMapping("all")
    public AuditItemWithPagination auditFindAll(Authentication authentication, //
            RequestAuditItem request //
    ) {
        return apiAuditManagementService.auditFindAll(authentication.getName(), request);
    }

}
