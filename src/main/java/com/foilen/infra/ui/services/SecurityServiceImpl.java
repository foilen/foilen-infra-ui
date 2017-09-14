/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
 package com.foilen.infra.ui.services;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.foilen.infra.plugin.v1.core.base.resources.Machine;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.ui.db.dao.UserDao;
import com.foilen.infra.ui.db.domain.user.ApiUser;
import com.foilen.infra.ui.db.domain.user.User;

@Service
public class SecurityServiceImpl implements SecurityService {

    @Autowired
    private ApiUserService apiUserService;
    @Autowired
    private IPResourceService ipResourceService;
    @Autowired
    private UserDao userDao;

    @Override
    public boolean canCreateResource(String userId, Class<?> resourceType) {
        return isAdmin(userId);
    }

    @Override
    public boolean canCreateResource(String userId, String resourceType) {
        return isAdmin(userId);
    }

    @Override
    public boolean canManageMachine(String userId) {
        return isAdmin(userId);
    }

    @Override
    public boolean canManageMachine(String userId, String machineName) {
        return isAdmin(userId);
    }

    @Override
    public boolean canManageResource(String userId, Class<?> resourceType, String resourceName) {
        return isAdmin(userId);
    }

    @Override
    public boolean canManageResource(String userId, String resourceType, String resourceName) {
        return isAdmin(userId);
    }

    @Override
    public boolean canMonitorMachine(String userId, String machineName) {
        return isAdmin(userId);
    }

    @Override
    public boolean isAdmin() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context == null) {
            return true;
        }
        Authentication authentication = context.getAuthentication();
        if (authentication == null) {
            return true;
        }
        return isAdmin(authentication.getName());
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
    public List<String> listCanInstallOnMachine(String userId) {

        if (!isAdmin(userId)) {
            return Collections.emptyList();
        }

        return ipResourceService.resourceFindAll(ipResourceService.createResourceQuery(Machine.class)).stream() //
                .map(Machine::getName) //
                .collect(Collectors.toList());
    }

}
