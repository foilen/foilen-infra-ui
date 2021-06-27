/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.foilen.infra.api.model.resource.PartialLinkDetails;
import com.foilen.infra.api.model.resource.ResourceBucket;
import com.foilen.infra.api.model.resource.ResourceBucketsWithPagination;
import com.foilen.infra.api.model.resource.ResourceDetails;
import com.foilen.infra.api.model.resource.ResourceTypeDetails;
import com.foilen.infra.api.request.RequestChanges;
import com.foilen.infra.api.request.RequestResourceSearch;
import com.foilen.infra.api.response.ResponseResourceAppliedChanges;
import com.foilen.infra.api.response.ResponseResourceBucket;
import com.foilen.infra.api.response.ResponseResourceBuckets;
import com.foilen.infra.api.response.ResponseResourceTypesDetails;
import com.foilen.infra.plugin.core.system.mongodb.repositories.PluginResourceRepository;
import com.foilen.infra.plugin.core.system.mongodb.repositories.documents.PluginResource;
import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.resource.IPResourceDefinition;
import com.foilen.infra.plugin.v1.core.resource.IPResourceQuery;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.infra.ui.repositories.PluginResourceInUiRepository;
import com.foilen.infra.ui.repositories.documents.AbstractUser;
import com.foilen.mvc.ui.UiException;
import com.foilen.smalltools.restapi.model.ApiPagination;
import com.foilen.smalltools.restapi.model.FormResult;
import com.foilen.smalltools.tools.JsonTools;
import com.foilen.smalltools.tools.StringTools;
import com.google.common.base.Strings;
import com.google.common.collect.ComparisonChain;

@Service
public class ApiResourceManagementServiceImpl extends AbstractApiService implements ApiResourceManagementService {

    @Autowired
    private ConversionService conversionService;
    @Autowired
    private PluginResourceInUiRepository pluginResourceInUiRepository;
    @Autowired
    private PluginResourceRepository pluginResourceRepository;
    @Autowired
    private IPResourceService resourceService;
    @Autowired
    private ResourceManagementService resourceManagementService;

    @Override
    public ResponseResourceAppliedChanges applyChanges(String userId, RequestChanges changes) {

        ResponseResourceAppliedChanges formResult = new ResponseResourceAppliedChanges();

        wrapExecution(formResult, () -> {

            // Translate request to change context
            ChangesContext changesContext = new ChangesContext(resourceService);
            AtomicInteger position = new AtomicInteger();
            changes.getResourcesToAdd().forEach(it -> {
                IPResource resource = convert("resourcesToAdd", position.getAndIncrement(), it, formResult);
                if (resource != null) {
                    changesContext.resourceAdd(resource);
                }
            });
            position.set(0);
            changes.getResourcesToUpdate().forEach(it -> {
                int posContext = position.getAndIncrement();
                IPResource resourceInitial = getPersistedResource("resourcesToUpdate.resource", posContext, it.getResource(), formResult);
                IPResource resource = convert("resourcesToUpdate.updatedResource", posContext, it.getUpdatedResource(), formResult);
                if (resourceInitial != null && resource != null) {
                    changesContext.resourceUpdate(resourceInitial, resource);
                }
            });
            position.set(0);
            changes.getResourcesToDelete().forEach(it -> {
                int posContext = position.getAndIncrement();
                IPResource resource = getPersistedResource("resourcesToDelete", posContext, it, formResult);
                if (resource != null) {
                    changesContext.resourceDelete(resource);
                }
            });

            position.set(0);
            changes.getLinksToAdd().forEach(it -> {
                int posContext = position.getAndIncrement();
                IPResource fromResource = convert("linksToAdd.fromResource", posContext, it.getFromResource(), formResult);
                String linkType = it.getLinkType();
                if (Strings.isNullOrEmpty(linkType)) {
                    formResult.getGlobalErrors().add("[linksToAdd.linkType/" + posContext + "] Is mandatory");
                }
                IPResource toResource = convert("linksToAdd.toResource", posContext, it.getToResource(), formResult);
                if (fromResource != null && toResource != null && !Strings.isNullOrEmpty(linkType)) {
                    changesContext.linkAdd(fromResource, linkType, toResource);
                }
            });
            position.set(0);
            changes.getLinksToDelete().forEach(it -> {
                int posContext = position.getAndIncrement();
                IPResource fromResource = convert("linksToDelete.fromResource", posContext, it.getFromResource(), formResult);
                String linkType = it.getLinkType();
                if (Strings.isNullOrEmpty(linkType)) {
                    formResult.getGlobalErrors().add("[linksToAdd.linkType/" + posContext + "] Is mandatory");
                }
                IPResource toResource = convert("linksToDelete.toResource", posContext, it.getToResource(), formResult);
                if (fromResource != null && toResource != null && !Strings.isNullOrEmpty(linkType)) {
                    changesContext.linkDelete(fromResource, linkType, toResource);
                }
            });

            position.set(0);
            changes.getTagsToAdd().forEach(it -> {
                int posContext = position.getAndIncrement();
                IPResource resource = convert("tagsToAdd.resource", posContext, it.getResource(), formResult);
                String tagName = it.getTagName();
                if (Strings.isNullOrEmpty(tagName)) {
                    formResult.getGlobalErrors().add("[tagsToAdd.tagName/" + posContext + "] Is mandatory");
                }
                if (resource != null && !Strings.isNullOrEmpty(tagName)) {
                    changesContext.tagAdd(resource, tagName);
                }
            });
            position.set(0);
            changes.getTagsToDelete().forEach(it -> {
                int posContext = position.getAndIncrement();
                IPResource resource = convert("tagsToDelete.resource", posContext, it.getResource(), formResult);
                String tagName = it.getTagName();
                if (Strings.isNullOrEmpty(tagName)) {
                    formResult.getGlobalErrors().add("[tagsToDelete.tagName/" + posContext + "] Is mandatory");
                }
                if (resource != null && !Strings.isNullOrEmpty(tagName)) {
                    changesContext.tagDelete(resource, tagName);
                }
            });

            // If no errors, execute
            if (formResult.isSuccess()) {
                try {
                    resourceManagementService.changesExecute(changesContext, changes.getDefaultOwner(), formResult);
                } catch (Exception e) {
                    formResult.getGlobalErrors().add("Problem executing the update: " + e.getMessage());
                }
            }

        });

        return formResult;
    }

    @Override
    public ResponseResourceAppliedChanges applyChangesAs(String userId, String impersonateUserId, RequestChanges changes) {

        entitlementService.canImpersonateOrFailUi(userId);

        // Switch user
        Authentication previousAuthentication = SecurityContextHolder.getContext().getAuthentication();
        AbstractUser impersonatedUser = entitlementService.getUser(impersonateUserId);
        if (impersonatedUser == null) {
            throw new UiException("error.userNotExist");
        }
        SecurityContextHolder.getContext().setAuthentication(impersonatedUser.toAuthentication());

        try {

            ResponseResourceAppliedChanges result = applyChanges(impersonateUserId, changes);
            if (result.isSuccess()) {
                auditingService.addImpersonatorToTransaction(result.getTxId(), userId);
            }

            return result;
        } finally {
            // Revert user
            SecurityContextHolder.getContext().setAuthentication(previousAuthentication);
        }

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

    protected IPResource convert(String context, int posContext, ResourceDetails resourceDetails, FormResult formResult) {

        String fullContext = "[" + context + "/" + posContext + "] ";

        // Get the type
        String resourceType = resourceDetails.getResourceType();
        if (Strings.isNullOrEmpty(resourceType)) {
            formResult.getGlobalErrors().add(fullContext + "resourceType is mandatory");
            return null;
        }

        // Get the class for that type
        Class<?> resourceClass = classFromResourceType(resourceType);
        if (resourceClass == null) {
            formResult.getGlobalErrors().add(fullContext + "Could not find a resource class for resource type [" + resourceType + "]");
            return null;
        }

        // Deserialize
        try {
            IPResource resource = (IPResource) JsonTools.clone(resourceDetails.getResource(), resourceClass);
            resource.setInternalId(resourceDetails.getResourceId());
            return resource;
        } catch (Exception e) {
            formResult.getGlobalErrors().add(fullContext + "could not deserialize the resource as type [" + resourceType + "]");
            return null;
        }
    }

    private ResourceBucket createResourceBucket(IPResource resource, boolean limitDetails) {
        ResourceBucket resourceBucket = new ResourceBucket();
        resourceBucket.setResourceDetails(createResourceDetails(resource, limitDetails));
        resourceService.linkFindAllRelatedByResource(resource).stream() //
                .sorted((a, b) -> ComparisonChain.start() //
                        .compare(a.getA().getResourceName(), b.getA().getResourceName()) //
                        .compare(a.getB(), b.getB()) //
                        .compare(a.getC().getResourceName(), b.getC().getResourceName()) //
                        .result())//
                .forEach(link -> {
                    if (StringTools.safeEquals(link.getA().getInternalId(), resource.getInternalId())) {
                        resourceBucket.getLinksTo().add(new PartialLinkDetails(createResourceDetails(link.getC(), true), link.getB()));
                    } else {
                        resourceBucket.getLinksFrom().add(new PartialLinkDetails(createResourceDetails(link.getA(), true), link.getB()));
                    }
                });
        resourceBucket.setTags(resourceService.tagFindAllByResource(resource).stream().sorted().collect(Collectors.toList()));
        return resourceBucket;
    }

    private ResourceDetails createResourceDetails(IPResource resource, boolean limitDetails) {
        String resourceType = resourceService.getResourceDefinition(resource).getResourceType();
        if (limitDetails) {
            Map<String, String> limitedResource = new HashMap<>();
            limitedResource.put("internalId", resource.getInternalId());
            limitedResource.put("resourceName", resource.getResourceName());
            limitedResource.put("resourceDescription", resource.getResourceDescription());
            limitedResource.put("resourceEditorName", resource.getResourceEditorName());
            return new ResourceDetails(resource.getInternalId(), resourceType, limitedResource);
        } else {
            return new ResourceDetails(resource.getInternalId(), resourceType, resource);
        }
    }

    private ResourceBucket createResourceNoLinks(IPResource resource, boolean limitDetails) {
        ResourceBucket resourceBucket = new ResourceBucket();
        resourceBucket.setResourceDetails(createResourceDetails(resource, limitDetails));
        resourceBucket.setTags(resourceService.tagFindAllByResource(resource).stream().sorted().collect(Collectors.toList()));
        return resourceBucket;
    }

    private IPResource getPersistedResource(String context, int posContext, ResourceDetails resourceDetails, FormResult formResult) {

        // Get by id
        if (!Strings.isNullOrEmpty(resourceDetails.getResourceId())) {
            Optional<IPResource> optional = resourceService.resourceFind(resourceDetails.getResourceId());
            if (!optional.isPresent()) {
                formResult.getGlobalErrors().add("[" + context + "/" + posContext + "] The resource does not exist");
                return null;
            }
            return optional.get();
        }

        // Search by PK
        IPResource resource = convert(context, posContext, resourceDetails, formResult);
        if (resource == null) {
            return null;
        }

        Optional<IPResource> optional = resourceService.resourceFindByPk(resource);
        if (!optional.isPresent()) {
            formResult.getGlobalErrors().add("[" + context + "/" + posContext + "] The resource does not exist");
            return null;
        }
        return optional.get();
    }

    @Override
    public ResponseResourceAppliedChanges resourceDelete(String userId, String resourceId) {
        RequestChanges changes = new RequestChanges();
        changes.getResourcesToDelete().add(new ResourceDetails(resourceId, null));
        return applyChanges(userId, changes);
    }

    @Override
    public ResourceBucketsWithPagination resourceFindAll(String userId, int pageId, String search, boolean onlyWithEditor) {

        ResourceBucketsWithPagination result = new ResourceBucketsWithPagination();

        // Parameters
        if (pageId < 1) {
            throw new UiException("error.pageStart1");
        }

        wrapExecution(result, () -> {

            result.setSearch(search);
            result.setOnlyWithEditor(onlyWithEditor);

            Page<IPResource> page = resourceManagementService.resourceFindAll(userId, pageId, search, onlyWithEditor);

            result.setItems(page.stream() //
                    .map(resource -> createResourceBucket(resource, false)) //
                    .collect(Collectors.toList()));

            result.setPagination(new ApiPagination(page));

        });

        return result;

    }

    @Override
    public ResponseResourceBuckets resourceFindAll(String userId, RequestResourceSearch resourceSearch) {

        ResponseResourceBuckets responseResourceBuckets = new ResponseResourceBuckets();

        wrapExecution(responseResourceBuckets, () -> {
            IPResourceQuery<IPResource> query = resourceService.createResourceQuery(resourceSearch.getResourceType());

            if (resourceSearch.getProperties() != null) {
                resourceSearch.getProperties().forEach((name, value) -> query.propertyEquals(name, value));
            }

            if (!Strings.isNullOrEmpty(resourceSearch.getTag())) {
                query.tagAddAnd(resourceSearch.getTag());
            }

            responseResourceBuckets.setItems(resourceManagementService.resourceFindAll(userId, query).stream() //
                    .map(resource -> createResourceBucket(resource, true)) //
                    .collect(Collectors.toList()));
        });

        return responseResourceBuckets;
    }

    @Override
    public ResourceBucketsWithPagination resourceFindAllAs(String userId, String impersonateUserId, int pageId, String search, boolean onlyWithEditor) {
        entitlementService.canImpersonateOrFailUi(userId);
        return resourceFindAll(impersonateUserId, pageId, search, onlyWithEditor);
    }

    @Override
    public ResponseResourceBuckets resourceFindAllAs(String userId, String impersonateUserId, RequestResourceSearch resourceSearch) {
        entitlementService.canImpersonateOrFailUi(userId);
        return resourceFindAll(impersonateUserId, resourceSearch);
    }

    @Override
    public ResponseResourceBuckets resourceFindAllWithDetails(String userId, RequestResourceSearch resourceSearch) {

        ResponseResourceBuckets responseResourceBuckets = new ResponseResourceBuckets();

        wrapExecution(responseResourceBuckets, () -> {
            IPResourceQuery<IPResource> query = resourceService.createResourceQuery(resourceSearch.getResourceType());

            if (resourceSearch.getProperties() != null) {
                resourceSearch.getProperties().forEach((name, value) -> query.propertyEquals(name, value));
            }

            if (!Strings.isNullOrEmpty(resourceSearch.getTag())) {
                query.tagAddAnd(resourceSearch.getTag());
            }

            responseResourceBuckets.setItems(resourceManagementService.resourceFindAll(userId, query).stream() //
                    .map(resource -> createResourceBucket(resource, !entitlementService.canViewResources(userId, resource.getInternalId()))) //
                    .collect(Collectors.toList()));
        });

        return responseResourceBuckets;

    }

    @Override
    public ResponseResourceBuckets resourceFindAllWithDetailsAs(String userId, String impersonateUserId, RequestResourceSearch resourceSearch) {
        entitlementService.canImpersonateOrFailUi(userId);
        return resourceFindAllWithDetails(impersonateUserId, resourceSearch);
    }

    @Override
    public ResponseResourceBuckets resourceFindAllWithoutOwner(String userId) {
        ResponseResourceBuckets responseResourceBuckets = new ResponseResourceBuckets();

        wrapExecution(responseResourceBuckets, () -> {

            // Permission
            entitlementService.isAdminOrFailUi(userId);

            // Get them
            responseResourceBuckets.setItems(pluginResourceInUiRepository.findAllWithoutOwner().stream() //
                    .map(it -> it.getResource()) //
                    .sorted((a, b) -> StringTools.safeComparisonNullFirst(a.getResourceName(), b.getResourceName())) //
                    .map(resource -> createResourceNoLinks(resource, false)) //
                    .collect(Collectors.toList()));
        });

        return responseResourceBuckets;
    }

    @Override
    public ResponseResourceBucket resourceFindById(String userId, String resourceId) {
        ResponseResourceBucket responseResourceBucket = new ResponseResourceBucket();

        wrapExecution(responseResourceBucket, () -> {

            entitlementService.canViewResourcesOrFailUi(userId, resourceId);

            Optional<PluginResource> pluginResourceO = pluginResourceRepository.findById(resourceId);

            // Resource does not exist
            if (pluginResourceO.isEmpty()) {
                throw new UiException("error.forbidden");
            }
            PluginResource pluginResource = pluginResourceO.get();

            // Fill
            IPResource resource = pluginResource.getResource();
            responseResourceBucket.setItem(createResourceBucket(resource, false));

        });

        return responseResourceBucket;
    }

    @Override
    public ResponseResourceBucket resourceFindOne(String userId, RequestResourceSearch resourceSearch) {
        ResponseResourceBucket responseResourceBucket = new ResponseResourceBucket();

        wrapExecution(responseResourceBucket, () -> {
            IPResourceQuery<IPResource> query = resourceService.createResourceQuery(resourceSearch.getResourceType());
            BeanWrapper resourceBeanWrapper = new BeanWrapperImpl(resourceService.getResourceDefinition(resourceSearch.getResourceType()).getResourceClass());

            if (resourceSearch.getProperties() != null) {
                resourceSearch.getProperties().forEach((name, value) -> {
                    Class<?> propertyType = resourceBeanWrapper.getPropertyType(name);
                    if (value != null) {
                        if (conversionService.canConvert(value.getClass(), propertyType)) {
                            value = conversionService.convert(value, propertyType);
                        }
                    }
                    query.propertyEquals(name, value);
                });
            }

            if (!Strings.isNullOrEmpty(resourceSearch.getTag())) {
                query.tagAddAnd(resourceSearch.getTag());
            }

            Optional<IPResource> optional = resourceService.resourceFind(query);
            if (optional.isPresent()) {
                IPResource resource = optional.get();
                entitlementService.canViewResourcesOrFailUi(userId, resource.getInternalId());
                responseResourceBucket.setItem(createResourceBucket(resource, false));
            }

        });

        return responseResourceBucket;
    }

    @Override
    public ResponseResourceTypesDetails resourceTypeFindAll() {
        ResponseResourceTypesDetails responseResourceTypesDetails = new ResponseResourceTypesDetails();

        wrapExecution(responseResourceTypesDetails, () -> {
            responseResourceTypesDetails.setItems(resourceService.getResourceDefinitions().stream() //
                    .sorted((a, b) -> a.getResourceType().compareTo(b.getResourceType())) //
                    .map(it -> new ResourceTypeDetails( //
                            it.getResourceType(), //
                            it.isEmbedded(), //
                            it.getPrimaryKeyProperties().stream().sorted().collect(Collectors.toList()), //
                            it.getPropertyNames().stream().collect(Collectors.toList()))) //
                    .collect(Collectors.toList()));
        });

        return responseResourceTypesDetails;
    }

}
