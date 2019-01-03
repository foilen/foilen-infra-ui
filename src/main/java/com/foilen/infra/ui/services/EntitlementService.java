/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.List;

public interface EntitlementService {

    void canDeleteResourcesOrFailUi(String userId);

    boolean canGetSetupForMachine(String userId, String machineName);

    void canGetSetupForMachineOrFailUi(String userId, String machineName);

    boolean canManageAllMachines(String userId);

    boolean canManageMachine(String userId, String machineName);

    boolean canMonitorMachine(String userId, String machineName);

    void canMonitorMachineOrFailUi(String userId, String machineName);

    void canUpdateResourcesOrFailUi(String userId);

    boolean isAdmin(String userId);

    void isAdminOrFailUi(String userId);

    boolean isTheMachine(String userId, String machineName);

    /**
     * Get the list of machines names where the user can install resources.
     *
     * @param userId
     *            the user id
     * @return the list of machines names
     */
    List<String> listCanInstallOnMachine(String userId);

}
