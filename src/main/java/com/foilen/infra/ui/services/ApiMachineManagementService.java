/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.List;

import com.foilen.infra.api.model.machine.SystemStats;
import com.foilen.infra.api.response.ResponseMachineSetup;
import com.foilen.smalltools.restapi.model.FormResult;

public interface ApiMachineManagementService {

    /**
     * Add system statistics if the currently logged in user can.
     *
     * @param userId
     *            the user
     * @param machineName
     *            the name of the machine
     * @param systemStats
     *            the stats
     * @return the success or error
     */
    FormResult addSystemStats(String userId, String machineName, List<SystemStats> systemStats);

    /**
     * Get the Machine Setup if the currently logged in user can.
     *
     * @param userId
     *            the user
     * @param machineName
     *            the name of the machine
     * @return the setup
     */
    ResponseMachineSetup getMachineSetup(String userId, String machineName);

    /**
     * Get the Machine Setup if the currently logged in user can.
     *
     * @param userId
     *            the user
     * @param machineName
     *            the name of the machine
     * @param ipPublic
     *            (Optional) the IP of the machine if known
     * @return the setup
     */
    ResponseMachineSetup getMachineSetup(String userId, String machineName, String ipPublic);

}
