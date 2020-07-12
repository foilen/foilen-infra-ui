/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import com.foilen.infra.ui.repositories.documents.AbstractUser;

public interface EntitlementService {

    boolean canGetSetupForMachine(String userId, String machineName);

    void canGetSetupForMachineOrFailUi(String userId, String machineName);

    boolean canManageAllMachines(String userId);

    boolean canManageMachine(String userId, String machineName);

    boolean canMonitorMachine(String userId, String machineName);

    void canMonitorMachineOrFailUi(String userId, String machineName);

    boolean canSendAlert(String userId);

    boolean canViewResources(String userId, String resourceId);

    void canViewResourcesOrFailUi(String userId, String resourceId);

    AbstractUser getUser(String userId);

    boolean isAdmin(String userId);

    void isAdminOrFailUi(String userId);

    boolean isTheMachine(String userId, String machineName);

}
