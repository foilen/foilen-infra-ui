/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.List;

import com.foilen.infra.api.model.MachineSetup;
import com.foilen.infra.resource.machine.Machine;

public interface MachineService {

    /**
     * Get the machine setup.
     *
     * @param machineName
     *            the machine's name
     * @return the machine setup or null if not present
     */
    MachineSetup getMachineSetup(String machineName);

    /**
     * List the machines that the user can edit.
     *
     * @param userId
     *            the user id
     * @return the machine names
     */
    List<String> list(String userId);

    List<Machine> listMachines(String userId);

    /**
     * List the machines that the user can monitor.
     *
     * @param userId
     *            the user id
     * @return the machine names
     */
    List<String> listMonitor(String userId);

    /**
     * Update the public ip on the machine.
     *
     * @param userId
     *            the user id
     * @param machineName
     *            the machine's name
     * @param ipPublic
     *            the public ip
     */
    void updateIpIfAvailable(String userId, String machineName, String ipPublic);

}
