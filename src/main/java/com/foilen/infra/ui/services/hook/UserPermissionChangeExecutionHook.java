/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services.hook;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.foilen.infra.api.model.permission.LinkAction;
import com.foilen.infra.api.model.permission.PermissionLink;
import com.foilen.infra.api.model.permission.PermissionResource;
import com.foilen.infra.api.model.permission.ResourceAction;
import com.foilen.infra.plugin.v1.core.eventhandler.changes.ChangeExecutionHook;
import com.foilen.infra.plugin.v1.core.eventhandler.changes.ChangesInTransactionContext;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.infra.ui.MetaConstants;
import com.foilen.infra.ui.repositories.OwnerRuleRepository;
import com.foilen.infra.ui.repositories.PluginResourceInUiRepository;
import com.foilen.infra.ui.repositories.documents.OwnerRule;
import com.foilen.infra.ui.services.UserPermissionsService;
import com.foilen.infra.ui.services.exception.UserPermissionException;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.StringTools;

public class UserPermissionChangeExecutionHook extends AbstractBasics implements ChangeExecutionHook {

    @Autowired
    private OwnerRuleRepository ownerRuleRepository;
    @Autowired
    private PluginResourceInUiRepository pluginResourceInUiRepository;
    @Autowired
    private IPResourceService resourceService;
    @Autowired
    private UserPermissionsService userPermissionsService;

    private void changeOwnerByRule(IPResource resource) {
        String resourceName = resource.getResourceName();
        logger.info("[checkOwnerRule] Resource: {}", resourceName);
        Optional<OwnerRule> ownerRule = ownerRuleRepository.findAll().stream().filter(rule -> {
            if (rule.getResourceNameStartsWith() != null) {
                if (!resourceName.startsWith(rule.getResourceNameStartsWith())) {
                    return false;
                }
            }
            if (rule.getResourceNameEndsWith() != null) {
                if (!resourceName.endsWith(rule.getResourceNameEndsWith())) {
                    return false;
                }
            }
            return true;
        }) //
                .findAny();
        if (ownerRule.isPresent()) {
            OwnerRule rule = ownerRule.get();
            logger.info("[checkOwnerRule] Resource: {} ; Got rule: {}", resourceName, rule);
            updateOwnership(resource, rule.getAssignOwner());
        }

    }

    protected void checkLink(ChangesInTransactionContext changesInTransactionContext, LinkAction action, boolean isExplicitChange, String fromType, String fromOwner, IPResource fromResource,
            String linkType, String toType, String toOwner, IPResource toResource) {

        String fromResourceName = fromResource.getResourceName();
        String toResourceName = toResource.getResourceName();

        logger.info("[checkLink] {} ; isExplicitChange {} ; [{}/{}] ({}) -> {} -> [{}/{}] ({})", //
                action, isExplicitChange, //
                fromType, fromOwner, fromResourceName, //
                linkType, //
                toType, toOwner, toResourceName //
        );

        List<PermissionLink> permissionLinks = userPermissionsService.findLinkPermissions(changesInTransactionContext.getUserType(), changesInTransactionContext.getUserName());

        List<PermissionLink> partials = new ArrayList<>();
        for (PermissionLink permissionLink : permissionLinks) {

            if (permissionLink.getAction() != LinkAction.ALL && permissionLink.getAction() != action) {
                continue;
            }

            if (isExplicitChange) {
                if (!permissionLink.isExplicitChange()) {
                    continue;
                }
            }

            if (notPartialAndNotAll(permissionLink.getFromType())) {
                if (!StringTools.safeEquals(permissionLink.getFromType(), fromType)) {
                    continue;
                }
            }

            if (notPartialAndNotAll(permissionLink.getFromOwner())) {
                if (!StringTools.safeEquals(permissionLink.getFromOwner(), fromOwner)) {
                    continue;
                }
            }

            if (notPartialAndNotAll(permissionLink.getLinkType())) {
                if (!StringTools.safeEquals(permissionLink.getLinkType(), linkType)) {
                    continue;
                }
            }

            if (notPartialAndNotAll(permissionLink.getToType())) {
                if (!StringTools.safeEquals(permissionLink.getToType(), toType)) {
                    continue;
                }
            }

            if (notPartialAndNotAll(permissionLink.getToOwner())) {
                if (!StringTools.safeEquals(permissionLink.getToOwner(), toOwner)) {
                    continue;
                }
            }

            if (permissionLink.isPartial()) {
                // Got a partial permission
                logger.info("Partial permission: {}", permissionLink);
                partials.add(permissionLink);
            } else {
                // Got the needed permission
                logger.info("Full permission: {}", permissionLink);
                return;
            }

        }

        // Check partials
        if (!partials.isEmpty()) {
            PermissionLink sum = new PermissionLink();
            partials.forEach(p -> {
                if (p.getFromType() != null) {
                    sum.setFromType(p.getFromType());
                }
                if (p.getFromOwner() != null) {
                    sum.setFromOwner(p.getFromOwner());
                }
                if (p.getLinkType() != null) {
                    sum.setLinkType(p.getLinkType());
                }
                if (p.getToType() != null) {
                    sum.setToType(p.getToType());
                }
                if (p.getToOwner() != null) {
                    sum.setToOwner(p.getToOwner());
                }
            });

            if (!sum.isPartial()) {
                logger.info("Got permission from partials");
                return;
            }
        }

        logger.info("Missing link permission. User has permissions:");
        permissionLinks.forEach(i -> logger.info("\t{}", i));
        throw new UserPermissionException("Missing link permission: " + action + " isExplicitChange:" + isExplicitChange + " [" + fromType + "/" + fromOwner + " (" + fromResourceName + ") -> "
                + linkType + " -> " + toType + "/" + toOwner + " (" + toResourceName + ")]");

    }

    protected void checkResource(ChangesInTransactionContext changesInTransactionContext, ResourceAction action, boolean isExplicitChange, String type, String owner, String resourceName) {
        logger.info("[checkResource] {} ; isExplicitChange {} ; [{}/{}] ; Resource name : {}", //
                action, isExplicitChange, //
                type, owner, resourceName //
        );

        List<PermissionResource> permissionResources = userPermissionsService.findResourcePermissions(changesInTransactionContext.getUserType(), changesInTransactionContext.getUserName());

        List<PermissionResource> partials = new ArrayList<>();
        for (PermissionResource permissionResource : permissionResources) {

            if (permissionResource.getAction() != ResourceAction.ALL && permissionResource.getAction() != action) {
                continue;
            }

            if (isExplicitChange) {
                if (!permissionResource.isExplicitChange()) {
                    continue;
                }
            }

            if (notPartialAndNotAll(permissionResource.getType())) {
                if (!StringTools.safeEquals(permissionResource.getType(), type)) {
                    continue;
                }
            }

            if (notPartialAndNotAll(permissionResource.getOwner())) {
                if (!StringTools.safeEquals(permissionResource.getOwner(), owner)) {
                    continue;
                }
            }

            if (permissionResource.isPartial()) {
                // Got a partial permission
                logger.info("Partial permission: {}", permissionResource);
                partials.add(permissionResource);
            } else {
                // Got the needed permission
                logger.info("Full permission: {}", permissionResource);
                return;
            }

        }

        // Check partials
        if (!partials.isEmpty()) {
            PermissionResource sum = new PermissionResource();
            partials.forEach(p -> {
                if (p.getType() != null) {
                    sum.setType(p.getType());
                }
                if (p.getOwner() != null) {
                    sum.setOwner(p.getOwner());
                }
            });

            if (!sum.isPartial()) {
                logger.info("Got permission from partials");
                return;
            }
        }

        logger.info("Missing resource permission. User has permissions:");
        permissionResources.forEach(i -> logger.info("\t{}", i));
        throw new UserPermissionException(
                "Missing resource permission: " + action + " isExplicitChange:" + isExplicitChange + " [" + type + "/" + owner + "] For resource named: [" + resourceName + "]");

    }

    @Override
    public void linkAdded(ChangesInTransactionContext changesInTransactionContext, IPResource fromResource, String linkType, IPResource toResource) {
        String fromType = resourceService.getResourceDefinition(fromResource.getClass()).getResourceType();
        String fromOwner = fromResource.getMeta().get(MetaConstants.META_OWNER);
        String toType = resourceService.getResourceDefinition(toResource.getClass()).getResourceType();
        String toOwner = toResource.getMeta().get(MetaConstants.META_OWNER);

        checkLink(changesInTransactionContext, LinkAction.ADD, changesInTransactionContext.isExplicitChange(), //
                fromType, fromOwner, fromResource, //
                linkType, //
                toType, toOwner, toResource//
        );
    }

    @Override
    public void linkDeleted(ChangesInTransactionContext changesInTransactionContext, IPResource fromResource, String linkType, IPResource toResource) {
        String fromType = resourceService.getResourceDefinition(fromResource.getClass()).getResourceType();
        String fromOwner = fromResource.getMeta().get(MetaConstants.META_OWNER);
        String toType = resourceService.getResourceDefinition(toResource.getClass()).getResourceType();
        String toOwner = toResource.getMeta().get(MetaConstants.META_OWNER);

        checkLink(changesInTransactionContext, LinkAction.DELETE, changesInTransactionContext.isExplicitChange(), //
                fromType, fromOwner, fromResource, //
                linkType, //
                toType, toOwner, toResource //
        );
    }

    private boolean notPartialAndNotAll(String value) {
        return value != null && !StringTools.safeEquals("*", value);
    }

    @Override
    public void resourceAdded(ChangesInTransactionContext changesInTransactionContext, IPResource resource) {
        String type = resourceService.getResourceDefinition(resource.getClass()).getResourceType();

        // Change ownership by rules
        changeOwnerByRule(resource);

        String owner = resource.getMeta().get(MetaConstants.META_OWNER);
        if (owner == null) {
            // Default for the transaction
            owner = changesInTransactionContext.getVars().get(MetaConstants.META_OWNER);
            if (owner != null) {
                updateOwnership(resource, owner);
            }
        }

        checkResource(changesInTransactionContext, ResourceAction.ADD, changesInTransactionContext.isExplicitChange(), //
                type, owner, resource.getResourceName() //
        );
    }

    @Override
    public void resourceDeleted(ChangesInTransactionContext changesInTransactionContext, IPResource resource) {
        String type = resourceService.getResourceDefinition(resource.getClass()).getResourceType();
        String owner = resource.getMeta().get(MetaConstants.META_OWNER);
        checkResource(changesInTransactionContext, ResourceAction.DELETE, changesInTransactionContext.isExplicitChange(), //
                type, owner, resource.getResourceName() //
        );
    }

    @Override
    public void resourceUpdated(ChangesInTransactionContext changesInTransactionContext, IPResource previousResource, IPResource updatedResource) {
        String type = resourceService.getResourceDefinition(previousResource.getClass()).getResourceType();
        String previousOwner = previousResource.getMeta().get(MetaConstants.META_OWNER);
        checkResource(changesInTransactionContext, ResourceAction.UPDATE, changesInTransactionContext.isExplicitChange(), //
                type, previousOwner, previousResource.getResourceName() //
        );

        // Change ownership by rules
        changeOwnerByRule(updatedResource);

        String nextOwner = updatedResource.getMeta().get(MetaConstants.META_OWNER);
        if (nextOwner == null) {
            nextOwner = previousOwner;
            if (previousOwner != null) {
                updateOwnership(updatedResource, nextOwner);
            }
        }

        if (!StringTools.safeEquals(previousOwner, nextOwner)) {
            checkResource(changesInTransactionContext, ResourceAction.UPDATE, changesInTransactionContext.isExplicitChange(), //
                    type, nextOwner, updatedResource.getResourceName() //
            );
        }
    }

    @Override
    public void tagAdded(ChangesInTransactionContext changesInTransactionContext, IPResource resource, String tagName) {
        String type = resourceService.getResourceDefinition(resource.getClass()).getResourceType();
        String owner = resource.getMeta().get(MetaConstants.META_OWNER);
        checkResource(changesInTransactionContext, ResourceAction.UPDATE, changesInTransactionContext.isExplicitChange(), //
                type, owner, resource.getResourceName() //
        );
    }

    @Override
    public void tagDeleted(ChangesInTransactionContext changesInTransactionContext, IPResource resource, String tagName) {
        String type = resourceService.getResourceDefinition(resource.getClass()).getResourceType();
        String owner = resource.getMeta().get(MetaConstants.META_OWNER);
        checkResource(changesInTransactionContext, ResourceAction.UPDATE, changesInTransactionContext.isExplicitChange(), //
                type, owner, resource.getResourceName() //
        );
    }

    private void updateOwnership(IPResource resource, String owner) {
        resource.getMeta().put(MetaConstants.META_OWNER, owner);
        pluginResourceInUiRepository.updateOwner(resource.getInternalId(), owner);
    }

}
