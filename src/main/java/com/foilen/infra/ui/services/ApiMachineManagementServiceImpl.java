/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.foilen.infra.api.model.machine.SystemStats;
import com.foilen.infra.api.response.ResponseMachineSetup;
import com.foilen.smalltools.restapi.model.ApiError;
import com.foilen.smalltools.restapi.model.FormResult;

@Service
public class ApiMachineManagementServiceImpl extends AbstractApiService implements ApiMachineManagementService {

    protected static boolean isIp4AndPublic(String ipPublic) { // 165.22.228.192

        // Is IPv4
        int dots = 0;
        for (int i = 0; i < ipPublic.length(); ++i) {
            if (ipPublic.charAt(i) == '.') {
                ++dots;
            }
        }
        if (dots != 3) {
            return false;
        }

        // Is not public

        // 127.0.0.0 to 127.255.255.255
        if (ipPublic.startsWith("127.")) {
            return false;
        }

        // 10.0.0.0 to 10.255.255.255
        if (ipPublic.startsWith("10.")) {
            return false;
        }

        // 169.254.0.0 to 169.254.255.255
        if (ipPublic.startsWith("169.254.")) {
            return false;
        }

        // 192.168.0.0 to 192.168.255.255
        if (ipPublic.startsWith("192.168.")) {
            return false;
        }

        // 172.16.0.0 to 172.31.255.255
        if (ipPublic.startsWith("172.")) {
            String[] parts = ipPublic.split("\\.");
            int b = Integer.valueOf(parts[1]);
            if (b >= 16 && b <= 31) {
                return false;
            }
        }

        return true;
    }

    @Autowired
    private MachineStatisticsService machineStatisticsService;
    @Autowired
    private MachineService machineService;

    @Autowired
    private EntitlementService entitlementService;

    @Override
    public FormResult addSystemStats(String userId, String machineName, List<SystemStats> systemStats) {

        FormResult formResult = new FormResult();

        if (!entitlementService.canGetSetupForMachine(userId, machineName)) {
            formResult.getGlobalErrors().add("You are not allowed");
            return formResult;
        }

        wrapExecution(formResult, () -> machineStatisticsService.addStats(machineName, systemStats));

        return formResult;

    }

    @Override
    public ResponseMachineSetup getMachineSetup(String userId, String machineName) {
        return getMachineSetup(userId, machineName, null);
    }

    @Override
    public ResponseMachineSetup getMachineSetup(String userId, String machineName, String ipPublic) {

        logger.debug("getMachineSetup userId: {} ; machineName: {} ; ipPublic: {}", userId, machineName, ipPublic);

        ResponseMachineSetup response = new ResponseMachineSetup();

        if (!entitlementService.canGetSetupForMachine(userId, machineName)) {
            response.setError(new ApiError("You are not allowed"));
            return response;
        }

        // Update the IP if is the machine
        if (ipPublic != null) {

            if (isIp4AndPublic(ipPublic)) {
                logger.debug("IP is an IPv4 address and public");

                if (entitlementService.isTheMachine(userId, machineName)) {
                    logger.debug("The user is the machine");
                    machineService.updateIpIfAvailable(userId, machineName, ipPublic);
                }
            } else {
                logger.debug("IP is not an IPv4 address or public");
            }
        }

        // Get the machine setup
        wrapExecution(response, () -> {
            response.setItem(machineService.getMachineSetup(machineName));
            if (response.getItem() == null) {
                response.setError(new ApiError("The machine does not exist"));
            }
        });

        return response;

    }

}
