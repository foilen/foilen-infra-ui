/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.List;

public interface SecurityService {

    boolean canCreateResource(String userId, Class<?> resourceType);

    boolean canCreateResource(String userId, String resourceType);

    boolean canManageMachine(String userId);

    boolean canManageMachine(String userId, String machineName);

    boolean canManageResource(String userId, Class<?> resourceType, String resourceName);

    boolean canManageResource(String userId, String resourceType, String resourceName);

    boolean canMonitorMachine(String userId, String machineName);

    boolean isAdmin();

    boolean isAdmin(String userId);

    /**
     * Get the list of machines names where the user can install resources.
     *
     * @param userId
     *            the user id
     * @return the list of machines names
     */
    List<String> listCanInstallOnMachine(String userId);

}
