/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.ui.db.dao.UserDao;
import com.foilen.infra.ui.db.domain.user.ApiMachineUser;
import com.foilen.infra.ui.db.domain.user.ApiUser;
import com.foilen.infra.ui.db.domain.user.User;
import com.foilen.mvc.ui.UiException;
import com.foilen.smalltools.tools.StringTools;

@Service
public class EntitlementServiceImpl implements EntitlementService {

    @Autowired
    private ApiUserService apiUserService;
    @Autowired
    private IPResourceService ipResourceService;
    @Autowired
    private UserDao userDao;

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
    public void canUpdateResourcesOrFailUi(String userId) {
        if (!isAdmin(userId)) {
            throw new UiException("error.forbidden");
        }
    }

    @Override
    public boolean isAdmin(String userId) {
        ApiUser apiUser = apiUserService.findByUserIdAndActive(userId);
        if (apiUser != null) {
            return apiUser.isAdmin();
        }

        User user = userDao.findByUserId(userId);
        return user != null && user.isAdmin();
    }

    @Override
    public void isAdminOrFailUi(String userId) {
        if (!isAdmin(userId)) {
            throw new UiException("error.forbidden");
        }
    }

    private boolean isAdminOrMachine(String userId, String machineName) {
        ApiUser apiUser = apiUserService.findByUserIdAndActive(userId);
        if (apiUser != null) {
            if (apiUser.isAdmin()) {
                return true;
            }
            if (apiUser instanceof ApiMachineUser) {
                ApiMachineUser apiMachineUser = (ApiMachineUser) apiUser;
                if (StringTools.safeEquals(apiMachineUser.getMachineName(), machineName)) {
                    return true;
                }
            }
        }

        User user = userDao.findByUserId(userId);
        return user != null && user.isAdmin();
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
