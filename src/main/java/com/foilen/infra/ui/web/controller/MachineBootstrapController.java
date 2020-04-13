/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.foilen.infra.api.model.MachineSetup;
import com.foilen.infra.ui.services.EntitlementService;
import com.foilen.infra.ui.services.MachineService;

@Controller
@RequestMapping("machineBootstrap")
public class MachineBootstrapController {

    @Autowired
    private MachineService machineService;
    @Autowired
    private EntitlementService entitlementService;

    @GetMapping("list")
    public ModelAndView list(Authentication authentication) {
        ModelAndView modelAndView = new ModelAndView("machineBootstrap/list");
        modelAndView.addObject("machines", machineService.listMachines(authentication.getName()));
        return modelAndView;
    }

    @GetMapping("view/{machineName:.+}")
    public ModelAndView view(Authentication authentication, @PathVariable String machineName) {

        entitlementService.canGetSetupForMachineOrFailUi(authentication.getName(), machineName);

        ModelAndView modelAndView = new ModelAndView("machineBootstrap/view");
        MachineSetup machineSetup = machineService.getMachineSetup(machineName);
        modelAndView.addObject("machineName", machineName);
        modelAndView.addObject("uiApiBaseUrl", machineSetup.getUiApiBaseUrl());
        modelAndView.addObject("uiApiUserId", machineSetup.getUiApiUserId());
        modelAndView.addObject("uiApiUserKey", machineSetup.getUiApiUserKey());
        return modelAndView;
    }

}
