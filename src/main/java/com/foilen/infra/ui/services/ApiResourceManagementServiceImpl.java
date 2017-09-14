/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
 package com.foilen.infra.ui.services;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.foilen.infra.api.request.ChangesRequest;
import com.foilen.infra.api.request.ResourceDetails;
import com.foilen.infra.api.response.ResponseWithStatus;
import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.resource.IPResourceDefinition;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.core.service.internal.InternalChangeService;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.JsonTools;
import com.google.common.base.Strings;

@Service
public class ApiResourceManagementServiceImpl extends AbstractBasics implements ApiResourceManagementService {

    @Autowired
    private InternalChangeService internalChangeService;
    @Autowired
    private IPResourceService resourceService;
    @Autowired
    private SecurityService securityService;

    @Override
    public ResponseWithStatus applyChanges(ChangesRequest changesRequest) {
        ResponseWithStatus response = new ResponseWithStatus();

        if (!securityService.isAdmin()) {
            response.getErrors().add("You are not an admin");
            return response;
        }

        // Translate request to change context
        ChangesContext changesContext = new ChangesContext(resourceService);
        AtomicInteger position = new AtomicInteger();
        changesRequest.getResourcesToAdd().forEach(it -> {
            IPResource resource = convert("resourcesToAdd", position.getAndIncrement(), it, response);
            if (resource != null) {
                changesContext.resourceAdd(resource);
            }
        });
        position.set(0);
        changesRequest.getResourcesToUpdate().forEach(it -> {
            int posContext = position.getAndIncrement();
            IPResource resourcePk = getPersistedResourceByPk("resourcesToUpdate.resourcePk", posContext, it.getResourcePk(), response);
            IPResource resource = convert("resourcesToUpdate.updatedResource", posContext, it.getUpdatedResource(), response);
            if (resourcePk != null && resource != null) {
                changesContext.resourceUpdate(resourcePk, resource);
            }
        });
        position.set(0);
        changesRequest.getResourcesToDeletePk().forEach(it -> {
            int posContext = position.getAndIncrement();
            IPResource resourcePk = getPersistedResourceByPk("resourcesToDeletePk", posContext, it, response);
            if (resourcePk != null) {
                changesContext.resourceDelete(resourcePk);
            }
        });

        position.set(0);
        changesRequest.getLinksToAdd().forEach(it -> {
            int posContext = position.getAndIncrement();
            IPResource fromResourcePk = convert("linksToAdd.fromResourcePk", posContext, it.getFromResourcePk(), response);
            String linkType = it.getLinkType();
            if (Strings.isNullOrEmpty(linkType)) {
                response.addError("[linksToAdd.linkType/" + posContext + "] Is mandatory");
            }
            IPResource toResourcePk = convert("linksToAdd.toResourcePk", posContext, it.getToResourcePk(), response);
            if (fromResourcePk != null && toResourcePk != null && !Strings.isNullOrEmpty(linkType)) {
                changesContext.linkAdd(fromResourcePk, linkType, toResourcePk);
            }
        });
        position.set(0);
        changesRequest.getLinksToDelete().forEach(it -> {
            int posContext = position.getAndIncrement();
            IPResource fromResourcePk = convert("linksToDelete.fromResourcePk", posContext, it.getFromResourcePk(), response);
            String linkType = it.getLinkType();
            if (Strings.isNullOrEmpty(linkType)) {
                response.addError("[linksToAdd.linkType/" + posContext + "] Is mandatory");
            }
            IPResource toResourcePk = convert("linksToDelete.toResourcePk", posContext, it.getToResourcePk(), response);
            if (fromResourcePk != null && toResourcePk != null && !Strings.isNullOrEmpty(linkType)) {
                changesContext.linkDelete(fromResourcePk, linkType, toResourcePk);
            }
        });

        position.set(0);
        changesRequest.getTagsToAdd().forEach(it -> {
            int posContext = position.getAndIncrement();
            IPResource resourcePk = convert("tagsToAdd.resourcePk", posContext, it.getResourcePk(), response);
            String tagName = it.getTagName();
            if (Strings.isNullOrEmpty(tagName)) {
                response.addError("[tagsToAdd.tagName/" + posContext + "] Is mandatory");
            }
            if (resourcePk != null && !Strings.isNullOrEmpty(tagName)) {
                changesContext.tagAdd(resourcePk, tagName);
            }
        });
        position.set(0);
        changesRequest.getTagsToDelete().forEach(it -> {
            int posContext = position.getAndIncrement();
            IPResource resourcePk = convert("tagsToDelete.resourcePk", posContext, it.getResourcePk(), response);
            String tagName = it.getTagName();
            if (Strings.isNullOrEmpty(tagName)) {
                response.addError("[tagsToDelete.tagName/" + posContext + "] Is mandatory");
            }
            if (resourcePk != null && !Strings.isNullOrEmpty(tagName)) {
                changesContext.tagDelete(resourcePk, tagName);
            }
        });

        // If no errors, execute
        if (response.isSuccess()) {
            try {
                internalChangeService.changesExecute(changesContext);
            } catch (Exception e) {
                response.addError("Problem executing the update: " + e.getMessage());
            }
        }

        return response;
    }

    protected Class<?> classFromResourceType(String resourceType) {
        if (Strings.isNullOrEmpty(resourceType)) {
            return null;
        }

        IPResourceDefinition resourceDefinition = resourceService.getResourceDefinition(resourceType);
        if (resourceDefinition == null) {
            return null;
        }

        return resourceDefinition.getResourceClass();
    }

    protected IPResource convert(String context, int posContext, ResourceDetails resourceDetails, ResponseWithStatus responseWithStatus) {

        String fullContext = "[" + context + "/" + posContext + "] ";

        // Get the type
        String resourceType = resourceDetails.getResourceType();
        if (Strings.isNullOrEmpty(resourceType)) {
            responseWithStatus.addError(fullContext + "resourceType is mandatory");
            return null;
        }

        // Get the class for that type
        Class<?> resourceClass = classFromResourceType(resourceType);
        if (resourceClass == null) {
            responseWithStatus.addError(fullContext + "Could not find a resource class for resource type [" + resourceType + "]");
            return null;
        }

        // Deserialize
        try {
            return (IPResource) JsonTools.clone(resourceDetails.getResource(), resourceClass);
        } catch (Exception e) {
            responseWithStatus.addError(fullContext + "could not deserialize the resource as type [" + resourceType + "]");
            return null;
        }
    }

    private IPResource getPersistedResourceByPk(String context, int posContext, ResourceDetails resourcePkDetails, ResponseWithStatus response) {
        IPResource resourcePk = convert(context, posContext, resourcePkDetails, response);
        if (resourcePk == null) {
            return null;
        }
        Optional<IPResource> optional = resourceService.resourceFindByPk(resourcePk);
        if (optional.isPresent()) {
            response.addError("[" + context + "/" + posContext + "] The resource does not exist");
            return optional.get();
        }
        return null;
    }

}
