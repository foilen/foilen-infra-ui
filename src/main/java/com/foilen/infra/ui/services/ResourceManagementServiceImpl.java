/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foilen.infra.api.model.permission.PermissionResource;
import com.foilen.infra.api.model.permission.ResourceAction;
import com.foilen.infra.api.response.ResponseResourceAppliedChanges;
import com.foilen.infra.plugin.core.system.mongodb.repositories.PluginResourceRepository;
import com.foilen.infra.plugin.core.system.mongodb.repositories.documents.PluginResource;
import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.internal.InternalServicesContext;
import com.foilen.infra.plugin.v1.core.resource.IPResourceQuery;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.infra.ui.MetaConstants;
import com.foilen.infra.ui.services.hook.DefaultOwnerChangeExecutionHook;
import com.foilen.infra.ui.services.hook.FillResponseChangeExecutionHook;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.StringTools;
import com.google.common.base.Strings;

@Service
@Transactional
public class ResourceManagementServiceImpl extends AbstractBasics implements ResourceManagementService {

    @Autowired
    private AuditingService auditingService;
    @Autowired
    private InternalServicesContext internalServicesContext;
    @Autowired
    private PaginationService paginationService;
    @Autowired
    private PluginResourceRepository pluginResourceRepository;
    @Autowired
    private UserPermissionsService userPermissionsService;

    private Criteria applyPermissionsOnQuery(Query mongodbQuery, Iterable<PermissionResource> permissionsResources) {
        List<Criteria> combinaisons = new ArrayList<>();
        permissionsResources.forEach(permissionResource -> {

            boolean allTypes = StringTools.safeEquals(permissionResource.getType(), "*");
            boolean allOwners = StringTools.safeEquals(permissionResource.getOwner(), "*");

            // * / *
            if (allTypes && allOwners) {
                combinaisons.add(new Criteria());
                return;
            }

            // * / owner
            if (allTypes) {
                combinaisons.add(new Criteria("resource.meta." + MetaConstants.META_OWNER).is(permissionResource.getOwner()));
                return;
            }

            // type / *
            if (allOwners) {
                combinaisons.add(new Criteria("type").is(permissionResource.getType()));
                return;
            }

            // type / owner
            combinaisons.add(new Criteria().andOperator( //
                    new Criteria("type").is(permissionResource.getType()), //
                    new Criteria("resource.meta." + MetaConstants.META_OWNER).is(permissionResource.getOwner())));

        });
        Criteria orOperator = new Criteria().orOperator(combinaisons.toArray(new Criteria[combinaisons.size()]));
        mongodbQuery.addCriteria(orOperator);
        return orOperator;
    }

    @Override
    public void changesExecute(ChangesContext changes, String defaultOwner, ResponseResourceAppliedChanges responseResourceAppliedChanges) {
        internalServicesContext.getInternalChangeService().changesExecute(changes, Arrays.asList( //
                new DefaultOwnerChangeExecutionHook(defaultOwner), //
                new FillResponseChangeExecutionHook(auditingService, responseResourceAppliedChanges) //
        ));
    }

    @Override
    public Page<IPResource> resourceFindAll(String userId, int pageId, String search, boolean onlyWithEditor) {

        // Entitlement
        List<PermissionResource> permissionsResources = userPermissionsService.findListResourcePermissions(userId, ResourceAction.LIST);

        // User has no permission
        if (permissionsResources.isEmpty()) {
            return new PageImpl<>(Collections.emptyList());
        }

        Pageable pageable = PageRequest.of(pageId - 1, paginationService.getItemsPerPage(), Direction.ASC, "type", "resource.resourceName", "resource.resourceDescription");
        Page<PluginResource> page = pluginResourceRepository.findAll(pageable, mongoQuery -> {

            // Entitlement
            List<Criteria> orCriterias = new ArrayList<>();
            orCriterias.add(applyPermissionsOnQuery(new Query(), permissionsResources));

            // Add search
            if (!Strings.isNullOrEmpty(search)) {

                orCriterias.add(new Criteria().orOperator( //
                        new Criteria("type").regex("^.*" + search + ".*$", "i"), //
                        new Criteria("resourceName").regex("^.*" + search + ".*$", "i"), //
                        new Criteria("resourceDescription").regex("^.*" + search + ".*$", "i"), //
                        new Criteria("resource.meta.UI_OWNER").regex("^.*" + search + ".*$", "i") //
                ));

            }

            // Add onlyWithEditor
            if (onlyWithEditor) {
                mongoQuery.addCriteria(new Criteria("editorName").ne(null));
            }

            mongoQuery.addCriteria(new Criteria().andOperator(orCriterias.toArray(new Criteria[orCriterias.size()])));

        });
        return new PageImpl<>(page.getContent().stream().map(PluginResource::getResource).collect(Collectors.toList()), pageable, page.getTotalElements());
    }

    @Override
    public <T extends IPResource> List<T> resourceFindAll(String userId, IPResourceQuery<T> query) {

        List<PermissionResource> permissionsResources = userPermissionsService.findListResourcePermissions(userId, ResourceAction.LIST);

        // User has no permission
        if (permissionsResources.isEmpty()) {
            return new ArrayList<>();
        }

        return pluginResourceRepository.findAll(query, mongodbQuery -> {
            applyPermissionsOnQuery(mongodbQuery, permissionsResources);
        });
    }

}
