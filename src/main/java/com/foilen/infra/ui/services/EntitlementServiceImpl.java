/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.ui.repositories.UserApiMachineRepository;
import com.foilen.infra.ui.repositories.UserHumanRepository;
import com.foilen.infra.ui.repositories.documents.UserApi;
import com.foilen.infra.ui.repositories.documents.UserApiMachine;
import com.foilen.infra.ui.repositories.documents.UserHuman;
import com.foilen.mvc.ui.UiException;
import com.foilen.smalltools.tools.StringTools;

@Service
public class EntitlementServiceImpl implements EntitlementService {

    @Autowired
    private IPResourceService ipResourceService;
    @Autowired
    private UserApiService userApiService;
    @Autowired
    private UserApiMachineRepository userApiMachineRepository;
    @Autowired
    private UserHumanRepository userHumanRepository;

    @Override
    public void canDeleteResourcesOrFailUi(String userId) {
        if (!isAdmin(userId)) {
            throw new UiException("error.forbidden");
        }
    }

    @Override
    public boolean canGetSetupForMachine(String userId, String machineName) {
        return isAdminOrMachine(userId, machineName);
    }

    @Override
    public void canGetSetupForMachineOrFailUi(String userId, String machineName) {
        if (!isAdminOrMachine(userId, machineName)) {
            throw new UiException("error.forbidden");
        }
    }

    @Override
    public boolean canManageAllMachines(String userId) {
        return isAdmin(userId);
    }

    @Override
    public boolean canManageMachine(String userId, String machineName) {
        return isAdminOrMachine(userId, machineName);
    }

    @Override
    public boolean canMonitorMachine(String userId, String machineName) {
        return isAdmin(userId);
    }

    @Override
    public void canMonitorMachineOrFailUi(String userId, String machineName) {
        if (!canMonitorMachine(userId, machineName)) {
            throw new UiException("error.forbidden");
        }
    }

    @Override
    public boolean canSendAlert(String userId) {
        return isAMachine(userId);
    }

    @Override
    public void canUpdateResourcesOrFailUi(String userId) {
        if (!isAdmin(userId)) {
            throw new UiException("error.forbidden");
        }
    }

    @Override
    public boolean isAdmin(String userId) {
        UserApi apiUser = userApiService.findByUserIdAndActive(userId);
        if (apiUser != null) {
            return apiUser.isAdmin();
        }

        Optional<UserHuman> user = userHumanRepository.findById(userId);
        return user.isPresent() && user.get().isAdmin();
    }

    @Override
    public void isAdminOrFailUi(String userId) {
        if (!isAdmin(userId)) {
            throw new UiException("error.forbidden");
        }
    }

    private boolean isAdminOrMachine(String userId, String machineName) {
        if (userId == null) {
            return false;
        }
        UserApi apiUser = userApiService.findByUserIdAndActive(userId);
        if (apiUser == null) {
            Optional<UserApiMachine> apiMachineUser = userApiMachineRepository.findById(userId);
            if (apiMachineUser.isPresent() && StringTools.safeEquals(apiMachineUser.get().getMachineName(), machineName)) {
                return true;
            }
        } else {
            if (apiUser.isAdmin()) {
                return true;
            }
        }

        Optional<UserHuman> user = userHumanRepository.findById(userId);
        return user.isPresent() && user.get().isAdmin();
    }

    private boolean isAMachine(String userId) {
        return userApiMachineRepository.findById(userId) != null;
    }

    @Override
    public boolean isTheMachine(String userId, String machineName) {
        Optional<UserApiMachine> apiMachineUser = userApiMachineRepository.findById(userId);
        return apiMachineUser.isPresent() && StringTools.safeEquals(machineName, apiMachineUser.get().getMachineName());
    }

    @Override
    public List<String> listCanInstallOnMachine(String userId) {

        if (!isAdmin(userId)) {
            return Collections.emptyList();
        }

        return ipResourceService.resourceFindAll(ipResourceService.createResourceQuery(Machine.class)).stream() //
                .map(Machine::getName) //
                .collect(Collectors.toList());
    }

}
