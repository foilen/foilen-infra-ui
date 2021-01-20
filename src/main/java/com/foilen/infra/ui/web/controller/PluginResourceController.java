/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.web.controller;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.exception.IllegalUpdateException;
import com.foilen.infra.plugin.v1.core.exception.ResourcePrimaryKeyCollisionException;
import com.foilen.infra.plugin.v1.core.resource.IPResourceDefinition;
import com.foilen.infra.plugin.v1.core.service.IPPluginService;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.core.service.SecurityService;
import com.foilen.infra.plugin.v1.core.service.internal.InternalChangeService;
import com.foilen.infra.plugin.v1.core.visual.PageDefinition;
import com.foilen.infra.plugin.v1.core.visual.PageItem;
import com.foilen.infra.plugin.v1.core.visual.editor.ResourceEditor;
import com.foilen.infra.plugin.v1.core.visual.pageItem.field.HiddenFieldPageItem;
import com.foilen.infra.plugin.v1.core.visual.pageItem.field.SelectOptionsPageItem;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.infra.ui.MetaConstants;
import com.foilen.infra.ui.services.EntitlementService;
import com.foilen.infra.ui.services.ResourceManagementService;
import com.foilen.infra.ui.services.UserPermissionsService;
import com.foilen.infra.ui.services.exception.UserPermissionException;
import com.foilen.infra.ui.services.hook.DefaultOwnerChangeExecutionHook;
import com.foilen.infra.ui.visual.ResourceTypeAndDetails;
import com.foilen.infra.ui.web.controller.response.ResourceSuggestResponse;
import com.foilen.infra.ui.web.controller.response.ResourceUpdateResponse;
import com.foilen.mvc.ui.UiException;
import com.foilen.mvc.ui.UiSuccessErrorView;
import com.foilen.smalltools.reflection.ReflectionTools;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.JsonTools;
import com.foilen.smalltools.tuple.Tuple2;
import com.google.common.base.Strings;
import com.google.common.collect.ComparisonChain;

@Controller
@RequestMapping("pluginresources")
public class PluginResourceController extends AbstractBasics {

    public static final String RESOURCE_ID_FIELD = "_resourceId";
    public static final String VIEW_BASE_PATH = "pluginresources";

    @Autowired
    private CommonServicesContext commonServicesContext;
    @Autowired
    private EntitlementService entitlementService;
    @Autowired
    private IPResourceService resourceService;
    @Autowired
    private InternalChangeService internalChangeService;
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private IPPluginService ipPluginService;
    @Autowired
    private ResourceManagementService resourceManagementService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private UserPermissionsService userPermissionsService;

    @SuppressWarnings("unchecked")
    private void addOwnerField(String userId, PageDefinition pageDefinition, Locale locale, IPResource editedResource) {
        SelectOptionsPageItem ownerField = new SelectOptionsPageItem();
        ownerField.setLabel(messageSource.getMessage("term.owner", null, locale));
        ownerField.setFieldName("_owner");

        Set<String> potentialOwners = userPermissionsService.findOwnersThatUserCanCreateAs(userId);
        if (editedResource == null) {
            // It is a new resource -> use the first one available
            Optional<String> desiredOwner = potentialOwners.stream().sorted().findFirst();
            if (desiredOwner.isPresent()) {
                ownerField.setFieldValue(desiredOwner.get());
            }
        } else {
            // There is a resource -> use the current owner of it
            String currentOwner = editedResource.getMeta().get(MetaConstants.META_OWNER);
            if (currentOwner != null) {
                potentialOwners.add(currentOwner);
                ownerField.setFieldValue(currentOwner);
            }
        }
        potentialOwners.stream().sorted().forEach(o -> ownerField.getOptions().add(o));

        // Add as the first field
        ((List<PageItem>) pageDefinition.getPageItems()).add(0, ownerField);
    }

    @GetMapping("create/{editorName}")
    public ModelAndView create(@PathVariable("editorName") String editorName) {
        ModelAndView modelAndView = new ModelAndView(VIEW_BASE_PATH + "/create");

        Optional<ResourceEditor<?>> editor = ipPluginService.getResourceEditorByName(editorName);

        if (editor.isPresent()) {
            modelAndView.addObject("editorName", editorName);
            modelAndView.addObject("resourceType", editor.get().getForResourceType().getName());
        }

        return modelAndView;
    }

    @GetMapping("createPageDefinition/{editorName}")
    public ModelAndView createPageDefinition(Authentication authentication, @PathVariable("editorName") String editorName, HttpServletRequest httpServletRequest, Locale locale) {
        ModelAndView modelAndView = new ModelAndView(VIEW_BASE_PATH + "/resource");

        Optional<ResourceEditor<?>> editor = ipPluginService.getResourceEditorByName(editorName);

        if (editor.isPresent()) {
            @SuppressWarnings("unchecked")
            ResourceEditor<IPResource> resourceEditor = (ResourceEditor<IPResource>) editor.get();
            IPResource defaultResource = ReflectionTools.instantiate(resourceEditor.getForResourceType());
            PageDefinition pageDefinition = resourceEditor.providePageDefinition(commonServicesContext, defaultResource);

            // _editorName
            HiddenFieldPageItem editorNameField = new HiddenFieldPageItem();
            editorNameField.setFieldName("_editorName");
            editorNameField.setFieldValue(editorName);
            pageDefinition.addPageItem(editorNameField);

            // _csrf
            HiddenFieldPageItem csrfField = new HiddenFieldPageItem();
            csrfField.setFieldName(securityService.getCsrfParameterName());
            csrfField.setFieldValue(securityService.getCsrfValue(httpServletRequest));
            pageDefinition.addPageItem(csrfField);

            // Available owners and selected
            addOwnerField(authentication.getName(), pageDefinition, locale, null);

            modelAndView.addObject("pageDefinition", pageDefinition);
        } else {
            modelAndView.setViewName("error/single-partial");
            modelAndView.addObject("error", "error.editorNotFound");
        }

        return modelAndView;
    }

    @GetMapping("createPageDefinitionByType/{resourceType:.*}")
    public ModelAndView createPageDefinitionByType(Authentication authentication, @PathVariable("resourceType") String resourceType, HttpServletRequest httpServletRequest, Locale locale) {
        ModelAndView modelAndView = new ModelAndView(VIEW_BASE_PATH + "/resource");

        Class<?> resourceClass = ReflectionTools.safelyGetClass(resourceType);
        if (resourceClass == null) {
            modelAndView.setViewName("error/single-partial");
            modelAndView.addObject("error", "error.editorNotFound");
        }
        @SuppressWarnings("unchecked")
        List<String> editors = ipPluginService.getResourceEditorNamesByResourceType((Class<? extends IPResource>) resourceClass);

        if (editors.isEmpty()) {
            modelAndView.setViewName("error/single-partial");
            modelAndView.addObject("error", "error.editorNotFound");
        } else {
            return createPageDefinition(authentication, editors.get(0), httpServletRequest, locale);
        }

        return modelAndView;
    }

    @PostMapping("delete")
    public ModelAndView delete(Authentication authentication, @RequestParam("resourceId") String resourceId, RedirectAttributes redirectAttributes) {
        return new UiSuccessErrorView(redirectAttributes) //
                .setSuccessViewName("redirect:/" + VIEW_BASE_PATH + "/list") //
                .setErrorViewName("redirect:/" + VIEW_BASE_PATH + "/list") //
                .execute((ui, modelAndView) -> {
                    ChangesContext changes = new ChangesContext(resourceService);
                    changes.resourceDelete(resourceId);
                    internalChangeService.changesExecute(changes);
                });
    }

    @GetMapping("edit/{resourceId}")
    public ModelAndView edit(Authentication authentication, @PathVariable("resourceId") String resourceId, Locale locale) {

        ModelAndView modelAndView = new ModelAndView(VIEW_BASE_PATH + "/edit");

        try {

            entitlementService.canViewResourcesOrFailUi(authentication.getName(), resourceId);

            Optional<IPResource> resourceOptional = resourceService.resourceFind(resourceId);
            if (resourceOptional.isPresent()) {

                IPResource resource = resourceOptional.get();

                String editorName = resource.getResourceEditorName();
                List<String> editorNames = ipPluginService.getResourceEditorNamesByResourceType(resource.getClass());
                if (editorName == null) {
                    if (editorNames.isEmpty()) {
                        modelAndView.setViewName(VIEW_BASE_PATH + "/rawView");
                    } else {
                        editorName = editorNames.get(0);
                    }
                }

                modelAndView.addObject("editorName", editorName);
                modelAndView.addObject("resourceId", resourceId);
                modelAndView.addObject("resourceType", resource.getClass().getName());

                modelAndView.addObject("resourceName", resource.getResourceName());

                modelAndView.addObject("editorNames", editorNames);
            }
        } catch (UiException e) {
            modelAndView.setViewName("error/single-partial");
            modelAndView.addObject("error", messageSource.getMessage(e.getMessage(), e.getParams(), locale));
        }

        return modelAndView;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @GetMapping("editPageDefinition/{editorName}/{resourceId}")
    public ModelAndView editPageDefinition(Authentication authentication, @PathVariable("editorName") String editorName, @PathVariable("resourceId") String resourceId,
            HttpServletRequest httpServletRequest, Locale locale) {
        ModelAndView modelAndView = new ModelAndView(VIEW_BASE_PATH + "/resource");

        entitlementService.canViewResourcesOrFailUi(authentication.getName(), resourceId);

        Optional editorOptional = ipPluginService.getResourceEditorByName(editorName);

        if (editorOptional.isPresent()) {

            ResourceEditor editor = (ResourceEditor) editorOptional.get();

            Optional editedResourceOptional = resourceService.resourceFind(resourceId);

            if (editedResourceOptional.isPresent()) {

                IPResource editedResource = (IPResource) editedResourceOptional.get();

                PageDefinition pageDefinition = editor.providePageDefinition(commonServicesContext, editedResource);

                // _editorName
                HiddenFieldPageItem editorNameField = new HiddenFieldPageItem();
                editorNameField.setFieldName("_editorName");
                editorNameField.setFieldValue(editorName);
                pageDefinition.addPageItem(editorNameField);

                // _resourceId
                HiddenFieldPageItem resourceIdField = new HiddenFieldPageItem();
                resourceIdField.setFieldName(RESOURCE_ID_FIELD);
                resourceIdField.setFieldValue(String.valueOf(editedResource.getInternalId()));
                pageDefinition.addPageItem(resourceIdField);

                // _csrf
                HiddenFieldPageItem csrfField = new HiddenFieldPageItem();
                csrfField.setFieldName(securityService.getCsrfParameterName());
                csrfField.setFieldValue(securityService.getCsrfValue(httpServletRequest));
                pageDefinition.addPageItem(csrfField);

                // Available owners and selected
                addOwnerField(authentication.getName(), pageDefinition, locale, editedResource);

                modelAndView.addObject("pageDefinition", pageDefinition);
            } else {
                modelAndView.setViewName("error/single-partial");
                modelAndView.addObject("error", "error.resourceNotFound");
            }
        } else {
            modelAndView.setViewName("error/single-partial");
            modelAndView.addObject("error", "error.editorNotFound");
        }

        return modelAndView;
    }

    @GetMapping("list")
    public ModelAndView list(Authentication authentication, //
            @RequestParam(value = "s", required = false) String search //
    ) {

        ModelAndView modelAndView = new ModelAndView(VIEW_BASE_PATH + "/list");

        try {

            String searchLower = search == null ? null : search.toLowerCase();
            if (search != null) {
                search = StringEscapeUtils.escapeHtml4(search);
            }

            List<IPResourceDefinition> resourceDefinitions = resourceService.getResourceDefinitions();

            Stream<ResourceTypeAndDetails> resourcesTypeAndDetailsStream = Stream.of();

            for (IPResourceDefinition resourceDefinition : resourceDefinitions) {
                String resourceType = resourceDefinition.getResourceType();
                Stream<IPResource> resources = resourceManagementService.resourceFindAll(authentication.getName(), resourceService.createResourceQuery(resourceType)).stream();

                // Filter by search
                if (!Strings.isNullOrEmpty(searchLower)) {

                    resources = resources.filter(it -> {
                        boolean hasOneMatch = resourceType.toLowerCase().contains(searchLower);
                        if (it.getResourceName() != null) {
                            hasOneMatch |= it.getResourceName().toLowerCase().contains(searchLower);
                        }
                        if (it.getResourceDescription() != null) {
                            hasOneMatch |= it.getResourceDescription().toLowerCase().contains(searchLower);
                        }
                        return hasOneMatch;
                    });

                }

                resourcesTypeAndDetailsStream = Stream.concat(resourcesTypeAndDetailsStream, resources.map(it -> new ResourceTypeAndDetails(resourceType, it)));

            }

            // Sorting
            resourcesTypeAndDetailsStream = resourcesTypeAndDetailsStream.sorted((a, b) -> ComparisonChain.start() //
                    .compare(a.getType(), b.getType()) //
                    .compare(a.getResource().getResourceName(), b.getResource().getResourceName()) //
                    .compare(a.getResource().getResourceDescription(), b.getResource().getResourceDescription()) //
                    .result() //
            );

            List<ResourceTypeAndDetails> resourcesTypeAndDetails = resourcesTypeAndDetailsStream.collect(Collectors.toList());
            modelAndView.addObject("resourcesTypeAndDetails", resourcesTypeAndDetails);
            modelAndView.addObject("search", search);
        } catch (UiException e) {
            modelAndView.setViewName("error/single-partial");
            modelAndView.addObject("error", e.getMessage());
        }
        return modelAndView;

    }

    @GetMapping("rawView/{resourceId}")
    public ModelAndView rawView(Authentication authentication, @PathVariable("resourceId") String resourceId, HttpServletRequest httpServletRequest, Locale locale) {

        ModelAndView modelAndView = new ModelAndView(VIEW_BASE_PATH + "/rawResource");

        try {
            entitlementService.canViewResourcesOrFailUi(authentication.getName(), resourceId);

            Optional<IPResource> editedResourceOptional = resourceService.resourceFind(resourceId);

            if (editedResourceOptional.isPresent()) {

                IPResource editedResource = editedResourceOptional.get();

                modelAndView.addObject("resourceJson", JsonTools.prettyPrintWithoutNulls(editedResource));
            } else {
                modelAndView.setViewName("error/single-partial");
                modelAndView.addObject("error", "error.resourceNotFound");
            }
        } catch (UiException e) {
            modelAndView.setViewName("error/single-partial");
            modelAndView.addObject("error", messageSource.getMessage(e.getMessage(), e.getParams(), locale));
        }

        return modelAndView;
    }

    @ResponseBody
    @GetMapping("suggest/{resourceType:.+}")
    public List<ResourceSuggestResponse> suggest(Authentication authentication, @PathVariable("resourceType") Class<? extends IPResource> resourceType) {
        return resourceManagementService.resourceFindAll(authentication.getName(), //
                resourceService.createResourceQuery(resourceType) //
        ).stream() //
                .map(it -> new ResourceSuggestResponse(it.getInternalId(), it.getResourceName(), it.getResourceDescription())) //
                .sorted((a, b) -> a.getName().compareTo(b.getName())) //
                .collect(Collectors.toList());
    }

    @ResponseBody
    @GetMapping("suggestEditor/{resourceType:.+}")
    public List<String> suggestEditor(@PathVariable("resourceType") Class<? extends IPResource> resourceType) {
        return ipPluginService.getResourceEditorNamesByResourceType(resourceType);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @ResponseBody
    @PostMapping("update")
    public ResourceUpdateResponse update(Authentication authentication, @RequestParam Map<String, String> formValues, Locale locale) {

        ResourceUpdateResponse resourceUpdateResponse = new ResourceUpdateResponse();

        // Get the editor
        String editorName = formValues.get("_editorName");
        if (Strings.isNullOrEmpty(editorName)) {
            resourceUpdateResponse.setTopError(messageSource.getMessage("error.editorNotSpecified", null, locale));
            return resourceUpdateResponse;
        }
        Optional<ResourceEditor<?>> resourceEditorOptional = ipPluginService.getResourceEditorByName(editorName);
        if (resourceEditorOptional.isPresent()) {
            ResourceEditor resourceEditor = resourceEditorOptional.get();

            // Get the resource if is editing
            String resourceId = formValues.get(RESOURCE_ID_FIELD);
            boolean isUpdate = resourceId != null;
            IPResource resource = null;
            if (resourceId != null) {
                Optional<IPResource> resourceOptional = resourceService.resourceFind(resourceId);
                if (!resourceOptional.isPresent()) {
                    resourceUpdateResponse.setTopError(messageSource.getMessage("error.resourceNotFound", null, locale));
                    return resourceUpdateResponse;
                }

                resource = resourceOptional.get();

                // Basic validation (resource id is of the supported type for the editor)
                if (!resourceEditor.getForResourceType().isAssignableFrom(resource.getClass())) {
                    resourceUpdateResponse.setTopError(messageSource.getMessage("error.editorCannotEditResource", null, locale));
                    return resourceUpdateResponse;
                }

            }

            // Create empty resource if new
            if (resource == null) {
                Class<?> resourceType = resourceEditor.getForResourceType();
                try {
                    resource = (IPResource) resourceType.getConstructor().newInstance();
                } catch (UserPermissionException e) {
                    logger.error("Could not create an empty resource of type {}", resourceType, e);
                    resourceUpdateResponse.setTopError(e.getMessage());
                    return resourceUpdateResponse;
                } catch (IllegalUpdateException e) {
                    logger.error("Could not create an empty resource of type {}", resourceType, e);
                    resourceUpdateResponse.setTopError(e.getMessage());
                    return resourceUpdateResponse;
                } catch (Exception e) {
                    logger.error("Could not create an empty resource of type {}", resourceType, e);
                    resourceUpdateResponse.setTopError(messageSource.getMessage("error.internalError", new Object[] { e.getMessage() }, locale));
                    return resourceUpdateResponse;
                }
            }

            // Editor's formating
            resourceEditor.formatForm(commonServicesContext, formValues);
            resourceUpdateResponse.getFieldsValues().putAll(formValues);

            // Editor's validation
            List<Tuple2<String, String>> errors = resourceEditor.validateForm(commonServicesContext, formValues);

            // Errors: add to resourceUpdateResponse.getFieldsErrors()
            if (errors != null && !errors.isEmpty()) {
                for (Tuple2<String, String> error : errors) {
                    resourceUpdateResponse.getFieldsErrors().put(error.getA(), messageSource.getMessage(error.getB(), null, locale));
                }
                return resourceUpdateResponse;
            }

            // Get the owner and set it in the transaction
            String owner = formValues.get("_owner");
            if (Strings.isNullOrEmpty(owner)) {
                owner = null;
            }
            DefaultOwnerChangeExecutionHook defaultOwnerChangeExecutionHook = new DefaultOwnerChangeExecutionHook(owner);

            // No errors: save and give the redirection link if no issues
            String internalId;
            if (isUpdate) {

                // Update existing resource
                IPResource newResource = JsonTools.clone(resource);
                newResource.setInternalId(resource.getInternalId());
                newResource.getMeta().put(MetaConstants.META_OWNER, owner);

                try {
                    ChangesContext changesContext = new ChangesContext(resourceService);
                    resourceEditor.fillResource(commonServicesContext, changesContext, formValues, newResource);
                    newResource.setResourceEditorName(editorName);
                    changesContext.resourceUpdate(resourceId, newResource);
                    internalChangeService.changesExecute(changesContext, Collections.singletonList(defaultOwnerChangeExecutionHook));
                } catch (ResourcePrimaryKeyCollisionException e) {
                    logger.error("Problem saving the resource", e);
                    resourceUpdateResponse.setTopError(messageSource.getMessage("error.duplicateResource", null, locale));
                    return resourceUpdateResponse;
                } catch (UserPermissionException e) {
                    logger.error("Problem saving the resource", e);
                    resourceUpdateResponse.setTopError(e.getMessage());
                    return resourceUpdateResponse;
                } catch (IllegalUpdateException e) {
                    logger.error("Problem saving the resource", e);
                    resourceUpdateResponse.setTopError(e.getMessage());
                    return resourceUpdateResponse;
                } catch (Exception e) {
                    logger.error("Problem saving the resource", e);
                    resourceUpdateResponse.setTopError(messageSource.getMessage("error.internalError", new Object[] { e.getMessage() }, locale));
                    return resourceUpdateResponse;
                }

                internalId = newResource.getInternalId();

            } else {

                // Create a new resource

                try {
                    ChangesContext changesContext = new ChangesContext(resourceService);
                    resourceEditor.fillResource(commonServicesContext, changesContext, formValues, resource);
                    changesContext.resourceAdd(resource);
                    resource.setResourceEditorName(editorName);
                    resource.getMeta().put(MetaConstants.META_OWNER, owner);
                    internalChangeService.changesExecute(changesContext, Collections.singletonList(defaultOwnerChangeExecutionHook));
                    resource = resourceService.resourceFindByPk(resource).get();
                } catch (ResourcePrimaryKeyCollisionException e) {
                    logger.error("Problem saving the resource", e);
                    resourceUpdateResponse.setTopError(messageSource.getMessage("error.duplicateResource", null, locale));
                    return resourceUpdateResponse;
                } catch (UserPermissionException e) {
                    logger.error("Problem saving the resource", e);
                    resourceUpdateResponse.setTopError(e.getMessage());
                    return resourceUpdateResponse;
                } catch (IllegalUpdateException e) {
                    logger.error("Problem saving the resource", e);
                    resourceUpdateResponse.setTopError(e.getMessage());
                    return resourceUpdateResponse;
                } catch (Exception e) {
                    logger.error("Problem saving the resource", e);
                    resourceUpdateResponse.setTopError(messageSource.getMessage("error.internalError", new Object[] { e.getMessage() }, locale));
                    return resourceUpdateResponse;
                }

                internalId = resource.getInternalId();

            }

            // Redirect url
            resourceUpdateResponse.setSuccessResource(resource);
            resourceUpdateResponse.setSuccessResourceId(internalId);

        } else {
            resourceUpdateResponse.setTopError(messageSource.getMessage("error.editorNotFound", null, locale));
        }

        return resourceUpdateResponse;
    }

}
