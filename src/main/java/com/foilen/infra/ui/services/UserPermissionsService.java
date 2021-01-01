/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.List;
import java.util.Set;

import com.foilen.infra.api.model.permission.PermissionLink;
import com.foilen.infra.api.model.permission.PermissionResource;
import com.foilen.infra.api.model.permission.ResourceAction;
import com.foilen.infra.plugin.v1.core.eventhandler.changes.AuditUserType;

public interface UserPermissionsService {

    List<PermissionLink> findLinkPermissions(AuditUserType userType, String userId);

    /**
     * Find all the owners that the users can do the action.
     *
     * @param userId
     *            the user
     * @return the list of resource types and owner
     */
    List<PermissionResource> findListResourcePermissions(String userId, ResourceAction filterByAction);

    Set<String> findOwnersThatUserCanCreateAs(String userId);

    List<PermissionResource> findResourcePermissions(AuditUserType userType, String userId);

}
