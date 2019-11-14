/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.web.controller.api;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foilen.infra.api.model.SystemStats;
import com.foilen.infra.api.response.ResponseMachineSetup;
import com.foilen.infra.ui.services.ApiMachineManagementService;
import com.foilen.smalltools.restapi.model.FormResult;
import com.foilen.smalltools.tools.AbstractBasics;
import com.google.common.base.Strings;

@RequestMapping(value = "api/machine", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
@RestController
@SwaggerExpose
public class MachineApiController extends AbstractBasics {

    @Autowired
    private ApiMachineManagementService apiMachineManagementService;

    @PostMapping("{machineName:.+}/systemStats")
    public FormResult addSystemStats(Authentication authentication, @PathVariable String machineName, @RequestBody List<SystemStats> systemStats) {
        return apiMachineManagementService.addSystemStats(authentication.getName(), machineName, systemStats);
    }

    @GetMapping("{machineName:.+}/setup")
    public ResponseMachineSetup setup(HttpServletRequest httpServletRequest, Authentication authentication, @PathVariable String machineName) {
        String ip = httpServletRequest.getHeader("X-Forwarded-For");
        if (Strings.isNullOrEmpty(ip)) {
            ip = httpServletRequest.getRemoteAddr();
            logger.debug("IP from remote address {}", ip);
        } else {
            logger.debug("IP from X-Forwarded-For {}", ip);
        }
        return apiMachineManagementService.getMachineSetup(authentication.getName(), machineName, ip);
    }

}
