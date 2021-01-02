/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foilen.infra.api.model.permission.LinkAction;
import com.foilen.infra.api.model.permission.PermissionLink;
import com.foilen.infra.api.model.permission.PermissionResource;
import com.foilen.infra.api.model.permission.ResourceAction;
import com.foilen.infra.plugin.v1.core.eventhandler.changes.AuditUserType;
import com.foilen.infra.ui.repositories.AllOwnersDefinedInRolesRepository;
import com.foilen.infra.ui.repositories.RoleRepository;
import com.foilen.infra.ui.repositories.documents.AbstractUser;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.JsonTools;
import com.foilen.smalltools.tools.StringTools;

@Service
@Transactional
public class UserPermissionsServiceImpl extends AbstractBasics implements UserPermissionsService {

    public static final PermissionResource ALL_PERMISSION_RESOURCE = new PermissionResource() //
            .setAction(ResourceAction.ALL) //
            .setExplicitChange(true) //
            .setOwner("*") //
            .setType("*") //
    ;

    public static final PermissionLink ALL_PERMISSION_LINK = new PermissionLink() //
            .setAction(LinkAction.ALL) //
            .setExplicitChange(true) //
            .setFromOwner("*") //
            .setFromType("*") //
            .setLinkType("*") //
            .setToOwner("*") //
            .setToType("*") //
    ;

    @Autowired
    private AllOwnersDefinedInRolesRepository allOwnersDefinedInRolesRepository;
    @Autowired
    private EntitlementService entitlementService;
    @Autowired
    private RoleRepository roleRepository;

    private Set<String> allOwners() {
        TreeSet<String> owners = allOwnersDefinedInRolesRepository.findAll(Sort.by("owner")).stream() //
                .map(it -> it.getOwner()) //
                .collect(Collectors.toCollection(() -> new TreeSet<>()));
        owners.add("");
        return owners;
    }

    @Override
    public List<PermissionLink> findLinkPermissions(AuditUserType userType, String userId) {

        List<PermissionLink> permissionLink = new ArrayList<>();

        // System
        if (userType == AuditUserType.SYSTEM) {
            permissionLink.add(JsonTools.clone(ALL_PERMISSION_LINK));
            return permissionLink;
        }

        // User
        AbstractUser user = entitlementService.getUser(userId);
        if (user == null) {
            return permissionLink;
        }

        if (user.isAdmin()) {
            permissionLink.add(JsonTools.clone(ALL_PERMISSION_LINK));
        } else {
            permissionLink.addAll(roleRepository.findAllByNameIn(user.getRoles()).flatMap(it -> it.getLinks().stream()).collect(Collectors.toList()));
        }

        return permissionLink;
    }

    @Override
    public List<PermissionResource> findListResourcePermissions(String userId, ResourceAction filterByAction) {
        List<PermissionResource> permissionResources = findResourcePermissions(null, userId);

        // Keep only the desired action and not partial
        permissionResources.removeIf(pr -> (pr.getAction() != ResourceAction.ALL && pr.getAction() != filterByAction) //
                || pr.isPartial());

        return permissionResources;
    }

    @Override
    public Set<String> findOwnersThatUserCanCreateAs(String userId) {

        Set<String> owners = new TreeSet<>();

        AbstractUser user = entitlementService.getUser(userId);
        if (user == null) {
            return owners;
        }

        if (user.isAdmin()) {
            // Find all owners
            owners.addAll(allOwners());
        } else {
            roleRepository.findAllById(user.getRoles()).forEach(role -> {
                role.getResources().forEach(resource -> {
                    String owner = resource.getOwner();
                    if (owner == null || !resource.isExplicitChange()) {
                        return;
                    }
                    if (resource.getAction() == ResourceAction.ALL || resource.getAction() == ResourceAction.VIEW) {
                        if (StringTools.safeEquals(owner, "*")) {
                            // Find all owners
                            owners.addAll(allOwners());
                            return;
                        } else {
                            owners.add(owner);
                        }
                    }
                });
            });
            ;
        }

        return owners;
    }

    @Override
    public List<PermissionResource> findResourcePermissions(AuditUserType userType, String userId) {

        List<PermissionResource> permissionResources = new ArrayList<>();

        // System
        if (userType == AuditUserType.SYSTEM) {
            permissionResources.add(JsonTools.clone(ALL_PERMISSION_RESOURCE));
            return permissionResources;
        }

        // User
        AbstractUser user = entitlementService.getUser(userId);
        if (user == null) {
            return permissionResources;
        }

        if (user.isAdmin()) {
            permissionResources.add(JsonTools.clone(ALL_PERMISSION_RESOURCE));
        } else {
            permissionResources.addAll(roleRepository.findAllByNameIn(user.getRoles()).flatMap(it -> it.getResources().stream()).collect(Collectors.toList()));
        }

        return permissionResources;
    }

}
