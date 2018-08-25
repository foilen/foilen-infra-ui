/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.foilen.infra.api.model.SystemStats;
import com.foilen.infra.api.response.ResponseMachineSetup;
import com.foilen.infra.api.response.ResponseWithStatus;

@Service
public class ApiMachineManagementServiceImpl extends AbstractApiService implements ApiMachineManagementService {

    @Autowired
    private MachineStatisticsService machineStatisticsService;
    @Autowired
    private MachineService machineService;
    @Autowired
    private EntitlementService entitlementService;

    @Override
    public ResponseWithStatus addSystemStats(String userId, String machineName, List<SystemStats> systemStats) {

        ResponseWithStatus response = new ResponseWithStatus();

        if (!entitlementService.canGetSetupForMachine(userId, machineName)) {
            response.addError("You are not allowed");
            return response;
        }

        wrapExecution(response, () -> machineStatisticsService.addStats(machineName, systemStats));

        return response;

    }

    @Override
    public ResponseMachineSetup getMachineSetup(String userId, String machineName) {
        return getMachineSetup(userId, machineName, null);
    }

    @Override
    public ResponseMachineSetup getMachineSetup(String userId, String machineName, String ipPublic) {

        ResponseMachineSetup response = new ResponseMachineSetup();

        if (!entitlementService.canGetSetupForMachine(userId, machineName)) {
            response.addError("You are not allowed");
            return response;
        }

        // Update the IP if is the machine
        if (ipPublic != null && entitlementService.isTheMachine(userId, machineName)) {
            machineService.updateIpIfAvailable(userId, machineName, ipPublic);
        }

        // Get the machine setup
        wrapExecution(response, () -> {
            response.setItem(machineService.getMachineSetup(machineName));
            if (response.getItem() == null) {
                response.addError("The machine does not exist");
            }
        });

        return response;

    }

}
