/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.foilen.infra.api.model.permission.ResourceAction;
import com.foilen.infra.plugin.core.system.mongodb.repositories.PluginResourceRepository;
import com.foilen.infra.plugin.core.system.mongodb.repositories.documents.PluginResource;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.ui.MetaConstants;
import com.foilen.infra.ui.repositories.UserApiMachineRepository;
import com.foilen.infra.ui.repositories.UserHumanRepository;
import com.foilen.infra.ui.repositories.documents.AbstractUser;
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
    private PluginResourceRepository pluginResourceRepository;
    @Autowired
    private ResourceManagementService resourceManagementService;
    @Autowired
    private UserApiService userApiService;
    @Autowired
    private UserApiMachineRepository userApiMachineRepository;
    @Autowired
    private UserHumanRepository userHumanRepository;
    @Autowired
    private UserPermissionsService userPermissionsService;

    @Override
    public boolean canGetSetupForMachine(String userId, String machineName) {

        if (isAdminOrMachine(userId, machineName)) {
            return true;
        }

        Optional<Machine> machine = ipResourceService.resourceFindByPk(new Machine(machineName));
        if (machine.isEmpty()) {
            return false;
        }

        return canViewResources(userId, machine.get().getInternalId());

    }

    @Override
    public void canGetSetupForMachineOrFailUi(String userId, String machineName) {
        if (!canGetSetupForMachine(userId, machineName)) {
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
        List<Machine> machines = resourceManagementService.resourceFindAll(userId, ipResourceService.createResourceQuery(Machine.class).primaryKeyEquals(new Machine(machineName)));
        return machines.size() == 1;
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
    public boolean canViewResources(String userId, String resourceId) {
        Optional<PluginResource> pluginResourceO = pluginResourceRepository.findById(resourceId);

        // Resource does not exist
        if (pluginResourceO.isEmpty()) {
            return false;
        }
        PluginResource pluginResource = pluginResourceO.get();

        boolean permitted = userPermissionsService.findListResourcePermissions(userId, ResourceAction.VIEW).stream() //
                .anyMatch(p -> (StringTools.safeEquals("*", p.getType()) || StringTools.safeEquals(p.getType(), pluginResource.getType())) //
                        && (StringTools.safeEquals("*", p.getOwner()) || StringTools.safeEquals(p.getOwner(), pluginResource.getResource().getMeta().get(MetaConstants.META_OWNER))) //
                );

        return permitted;
    }

    @Override
    public void canViewResourcesOrFailUi(String userId, String resourceId) {
        if (!canViewResources(userId, resourceId)) {
            throw new UiException("error.forbidden");
        }
    }

    @Override
    public AbstractUser getUser(String userId) {

        if (userId == null) {
            return null;
        }

        UserApi apiUser = userApiService.findByUserIdAndActive(userId);
        if (apiUser != null) {
            return apiUser;
        }

        Optional<UserApiMachine> apiMachineUser = userApiMachineRepository.findById(userId);
        if (apiMachineUser.isPresent()) {
            return apiMachineUser.get();
        }

        Optional<UserHuman> user = userHumanRepository.findById(userId);
        if (user.isPresent()) {
            return user.get();
        }

        return null;

    }

    @Override
    public boolean isAdmin(String userId) {
        if (userId == null) {
            return false;
        }

        AbstractUser user = getUser(userId);
        if (user == null) {
            return false;
        }

        return user.isAdmin();
    }

    @Override
    public void isAdminOrFailUi(String userId) {
        if (!isAdmin(userId)) {
            throw new UiException("error.notAdmin");
        }
    }

    private boolean isAdminOrMachine(String userId, String machineName) {
        if (userId == null) {
            return false;
        }

        AbstractUser user = getUser(userId);
        if (user == null) {
            return false;
        }

        if (user instanceof UserApiMachine) {
            return StringTools.safeEquals(((UserApiMachine) user).getMachineName(), machineName);
        }

        return user.isAdmin();
    }

    private boolean isAMachine(String userId) {
        return userApiMachineRepository.findById(userId) != null;
    }

    @Override
    public boolean isTheMachine(String userId, String machineName) {
        Optional<UserApiMachine> apiMachineUser = userApiMachineRepository.findById(userId);
        return apiMachineUser.isPresent() && StringTools.safeEquals(machineName, apiMachineUser.get().getMachineName());
    }

}
