/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.foilen.chart.Chart;
import com.foilen.infra.ui.services.EntitlementService;
import com.foilen.infra.ui.services.MachineService;
import com.foilen.infra.ui.services.MachineStatisticsService;

@Controller
@RequestMapping("machineGraphics")
public class MachineGraphicsController {

    @Autowired
    private MachineService machineService;
    @Autowired
    private MachineStatisticsService machineStatisticsService;
    @Autowired
    private EntitlementService entitlementService;

    @ResponseBody
    @GetMapping("graphCpu/{name:.+}")
    public Chart graphCpu(Authentication authentication, @PathVariable String name) {

        entitlementService.canMonitorMachineOrFailUi(authentication.getName(), name);

        return machineStatisticsService.getCpuChart(name);
    }

    @ResponseBody
    @GetMapping("graphDisk/{name:.+}")
    public Chart graphDisk(Authentication authentication, @PathVariable String name) {

        entitlementService.canMonitorMachineOrFailUi(authentication.getName(), name);

        return machineStatisticsService.getDiskChart(name);
    }

    @ResponseBody
    @GetMapping("graphMemory/{name:.+}")
    public Chart graphMemory(Authentication authentication, @PathVariable String name) {

        entitlementService.canMonitorMachineOrFailUi(authentication.getName(), name);

        return machineStatisticsService.getMemoryChart(name);
    }

    @ResponseBody
    @GetMapping("graphNetwork/{name:.+}")
    public Chart graphNetwork(Authentication authentication, @PathVariable String name) {

        entitlementService.canMonitorMachineOrFailUi(authentication.getName(), name);

        return machineStatisticsService.getNetworkChart(name);
    }

    @GetMapping("list")
    public ModelAndView listGraphs(Authentication authentication) {
        ModelAndView modelAndView = new ModelAndView("machineGraphics/list");
        modelAndView.addObject("machines", machineService.listMonitor(authentication.getName()));
        return modelAndView;
    }

    @GetMapping("view/{name:.+}")
    public ModelAndView view(Authentication authentication, @PathVariable String name) {
        ModelAndView modelAndView = new ModelAndView("machineGraphics/view");
        modelAndView.addObject("machineName", name);
        return modelAndView;
    }

}
