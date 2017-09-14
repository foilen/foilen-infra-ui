/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
 package com.foilen.infra.ui.services;

import java.util.List;

import com.foilen.infra.api.model.MachineSetup;

public interface MachineService {

    /**
     * Get the machine setup.
     *
     * @param name
     *            the machine's name
     * @return the machine setup
     */
    MachineSetup getMachineSetup(String name);

    /**
     * Get the machine setup.
     *
     * @param userId
     *            the user id
     * @param name
     *            the machine's name
     * @return the machine setup
     */
    MachineSetup getMachineSetup(String userId, String name);

    /**
     * List the machines that the user can edit.
     *
     * @param userId
     *            the user id
     * @return the machine names
     */
    List<String> list(String userId);

    /**
     * List the machines that the user can monitor.
     *
     * @param userId
     *            the user id
     * @return the machine names
     */
    List<String> listMonitor(String userId);

    /**
     * Check if the machine exists.
     *
     * @param name
     *            the machine's name
     * @return true if it exists
     */
    boolean machineExists(String name);

    /**
     * Update the public ip on the machine.
     *
     * @param name
     *            the machine's name
     * @param ipPublic
     *            the public ip
     */
    void updateIpIfAvailable(String name, String ipPublic);

}
