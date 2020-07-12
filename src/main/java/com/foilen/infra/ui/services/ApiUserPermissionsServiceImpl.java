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
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foilen.infra.api.model.permission.OwnerRuleCreateOrEditForm;
import com.foilen.infra.api.model.permission.OwnerRuleResult;
import com.foilen.infra.api.model.permission.OwnerRuleWithPagination;
import com.foilen.infra.api.model.permission.RoleCreateForm;
import com.foilen.infra.api.model.permission.RoleEditForm;
import com.foilen.infra.api.model.permission.RoleResult;
import com.foilen.infra.api.model.permission.RoleSmall;
import com.foilen.infra.api.model.permission.RoleSmallWithPagination;
import com.foilen.infra.api.model.user.UserApiNewFormResult;
import com.foilen.infra.api.model.user.UserApiWithPagination;
import com.foilen.infra.api.model.user.UserHumanWithPagination;
import com.foilen.infra.api.model.user.UserRoleEditForm;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.ui.repositories.OwnerRuleRepository;
import com.foilen.infra.ui.repositories.RoleRepository;
import com.foilen.infra.ui.repositories.UserApiRepository;
import com.foilen.infra.ui.repositories.UserHumanRepository;
import com.foilen.infra.ui.repositories.documents.OwnerRule;
import com.foilen.infra.ui.repositories.documents.Role;
import com.foilen.infra.ui.repositories.documents.UserApi;
import com.foilen.infra.ui.repositories.documents.UserHuman;
import com.foilen.mvc.ui.UiException;
import com.foilen.smalltools.restapi.model.FormResult;
import com.foilen.smalltools.tools.JsonTools;
import com.foilen.smalltools.tools.SecureRandomTools;
import com.foilen.smalltools.tools.StringTools;
import com.google.common.base.Strings;
import com.google.common.collect.ComparisonChain;

@Service
@Transactional
public class ApiUserPermissionsServiceImpl extends AbstractApiService implements ApiUserPermissionsService {

    @Autowired
    private IPResourceService resourceService;
    @Autowired
    private OwnerRuleRepository ownerRuleRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserApiRepository userApiRepository;
    @Autowired
    private UserHumanRepository userHumanRepository;

    @Override
    public FormResult ownerRuleAdd(String userId, OwnerRuleCreateOrEditForm form) {

        FormResult result = new FormResult();

        wrapExecution(result, () -> {

            // Permission
            entitlementService.isAdminOrFailUi(userId);

            // Fields
            validateAtLeastOneManadatory(result, new String[] { "resourceNameStartsWith", "resourceNameEndsWith" }, new String[] { form.getResourceNameEndsWith(), form.getResourceNameStartsWith() });
            validateManadatory(result, "assignOwner", form.getAssignOwner());
            validateAlphaNumExtra(result, "assignOwner", form.getAssignOwner());

            if (!result.isSuccess()) {
                return;
            }

            // Execute
            OwnerRule ownerRule = ownerRuleRepository.save(JsonTools.clone(form, OwnerRule.class));
            auditingService.documentAdd(userId, ownerRule);

        });

        return result;

    }

    @Override
    public FormResult ownerRuleDelete(String userId, String ownerRuleId) {

        FormResult result = new FormResult();

        wrapExecution(result, () -> {

            // Permission
            entitlementService.isAdminOrFailUi(userId);

            // Get
            Optional<OwnerRule> ownerRule = ownerRuleRepository.findById(ownerRuleId);
            if (ownerRule.isEmpty()) {
                result.getGlobalWarnings().add(translationService.translate("error.notExists"));
            } else {
                auditingService.documentDelete(userId, ownerRule.get());
                ownerRuleRepository.delete(ownerRule.get());
            }

        });

        return result;

    }

    @Override
    public FormResult ownerRuleEdit(String userId, String ownerRuleId, OwnerRuleCreateOrEditForm form) {

        FormResult result = new FormResult();

        wrapExecution(result, () -> {

            // Permission
            entitlementService.isAdminOrFailUi(userId);

            // Fields
            validateAtLeastOneManadatory(result, new String[] { "resourceNameStartsWith", "resourceNameEndsWith" }, new String[] { form.getResourceNameEndsWith(), form.getResourceNameStartsWith() });
            validateManadatory(result, "assignOwner", form.getAssignOwner());
            validateAlphaNumExtra(result, "assignOwner", form.getAssignOwner());

            // Get
            Optional<OwnerRule> existingOwnerRuleO = ownerRuleRepository.findById(ownerRuleId);
            if (existingOwnerRuleO.isEmpty()) {
                addValidationError(result, "ownerRuleId", "error.notExists");
                return;
            }
            OwnerRule updatedOwnerRule = existingOwnerRuleO.get();
            OwnerRule existingOwnerRule = JsonTools.clone(updatedOwnerRule);

            // Ensure it is different
            updatedOwnerRule.setAssignOwner(form.getAssignOwner());
            updatedOwnerRule.setResourceNameEndsWith(form.getResourceNameEndsWith());
            updatedOwnerRule.setResourceNameStartsWith(form.getResourceNameStartsWith());
            if (StringTools.safeEquals(JsonTools.compactPrintWithoutNulls(existingOwnerRule), JsonTools.compactPrintWithoutNulls(updatedOwnerRule))) {
                result.getGlobalWarnings().add(translationService.translate("warning.nochange"));
            }

            if (!result.isSuccess() || !result.getGlobalWarnings().isEmpty()) {
                return;
            }

            // Edit
            updatedOwnerRule = ownerRuleRepository.save(updatedOwnerRule);
            auditingService.documentEdit(userId, existingOwnerRule, updatedOwnerRule);

        });

        return result;

    }

    @Override
    public OwnerRuleWithPagination ownerRuleFindAll(String userId, int pageId) {

        OwnerRuleWithPagination results = new OwnerRuleWithPagination();
        wrapExecution(results, () -> {

            // Permission
            entitlementService.isAdminOrFailUi(userId);

            // Parameters
            if (pageId < 1) {
                throw new UiException("error.pageStart1");
            }

            // List
            Page<OwnerRule> page;
            page = ownerRuleRepository.findAll(PageRequest.of(pageId - 1, paginationService.getItemsPerPage(), Direction.ASC, "id"));

            paginationService.wrap(results, page, com.foilen.infra.api.model.permission.OwnerRule.class);

        });

        return results;

    }

    @Override
    public OwnerRuleResult ownerRuleFindOne(String userId, String ownerRuleId) {
        OwnerRuleResult result = new OwnerRuleResult();

        wrapExecution(result, () -> {

            // Permission
            entitlementService.isAdminOrFailUi(userId);

            // Get
            Optional<OwnerRule> role = ownerRuleRepository.findById(ownerRuleId);
            paginationService.wrap(result, role, com.foilen.infra.api.model.permission.OwnerRule.class);
        });

        return result;
    }

    @Override
    public FormResult roleAdd(String userId, RoleCreateForm form) {

        FormResult result = new FormResult();

        wrapExecution(result, () -> {

            // Fields
            validateManadatory(result, "name", form.getName());
            validateAlphaNumExtra(result, "name", form.getName());

            // Permission
            entitlementService.isAdminOrFailUi(userId);

            if (!result.isSuccess()) {
                return;
            }

            // Check already exists
            Optional<Role> existingRole = roleRepository.findById(form.getName());
            if (existingRole.isPresent()) {
                addValidationError(result, "name", "error.alreadyExists");
            }

            if (!result.isSuccess()) {
                return;
            }

            // Execute
            Role role = roleRepository.save(JsonTools.clone(form, Role.class));
            auditingService.documentAdd(userId, role);

        });

        return result;

    }

    @Override
    public FormResult roleDelete(String userId, String roleName) {

        FormResult result = new FormResult();

        wrapExecution(result, () -> {

            // Permission
            entitlementService.isAdminOrFailUi(userId);

            // Get
            Optional<Role> role = roleRepository.findById(roleName);
            if (role.isEmpty()) {
                result.getGlobalWarnings().add(translationService.translate("error.notExists"));
            } else {
                auditingService.documentDelete(userId, role.get());
                roleRepository.delete(role.get());
            }

        });

        return result;

    }

    @Override
    public FormResult roleEdit(String userId, String roleName, RoleEditForm form) {

        FormResult result = new FormResult();

        wrapExecution(result, () -> {

            // Permission
            entitlementService.isAdminOrFailUi(userId);

            // Ensure all permissions are well filled
            List<String> validTypes = resourceService.getResourceDefinitions().stream() //
                    .map(it -> it.getResourceType()) //
                    .collect(Collectors.toList());
            validTypes.add("*");
            boolean permissionIssue = form.getLinks().stream().anyMatch(it -> //
            it.getAction() == null //
                    || (!Strings.isNullOrEmpty(it.getFromType()) && !validTypes.contains(it.getFromType())) //
            );
            if (permissionIssue) {
                addValidationError(result, "links", "error.invalid");
            }

            permissionIssue = form.getResources().stream().anyMatch(it -> //
            it.getAction() == null //
                    || (!Strings.isNullOrEmpty(it.getType()) && !validTypes.contains(it.getType())) //
            );
            if (permissionIssue) {
                addValidationError(result, "resources", "error.invalid");
            }

            if (!result.isSuccess()) {
                return;
            }

            // Get
            Optional<Role> existingRoleO = roleRepository.findById(roleName);
            if (existingRoleO.isEmpty()) {
                addValidationError(result, "name", "error.notExists");
                return;
            }
            Role updatedRole = existingRoleO.get();
            Role existingRole = JsonTools.clone(updatedRole);

            // Remove all blanks
            form.getLinks().forEach(it -> {
                if (Strings.isNullOrEmpty(it.getFromOwner())) {
                    it.setFromOwner(null);
                }
                if (Strings.isNullOrEmpty(it.getFromType())) {
                    it.setFromType(null);
                }
                if (Strings.isNullOrEmpty(it.getLinkType())) {
                    it.setLinkType(null);
                }
                if (Strings.isNullOrEmpty(it.getToOwner())) {
                    it.setToOwner(null);
                }
                if (Strings.isNullOrEmpty(it.getToType())) {
                    it.setToType(null);
                }
            });
            form.getResources().forEach(it -> {
                if (Strings.isNullOrEmpty(it.getOwner())) {
                    it.setOwner(null);
                }
                if (Strings.isNullOrEmpty(it.getType())) {
                    it.setType(null);
                }
            });

            // Sort
            form.getLinks().sort((a, b) -> ComparisonChain.start() //
                    .compare(a.getAction(), b.getAction()) //
                    .compareTrueFirst(a.isExplicitChange(), b.isExplicitChange()) //
                    .compare(a.getFromType(), b.getFromType(), StringTools::safeComparisonNullLast) //
                    .compare(a.getFromOwner(), b.getFromOwner(), StringTools::safeComparisonNullLast) //
                    .compare(a.getLinkType(), b.getLinkType(), StringTools::safeComparisonNullLast) //
                    .compare(a.getToType(), b.getToType(), StringTools::safeComparisonNullLast) //
                    .compare(a.getToOwner(), b.getToOwner(), StringTools::safeComparisonNullLast) //
                    .result());
            form.getResources().sort((a, b) -> ComparisonChain.start() //
                    .compare(a.getAction(), b.getAction()) //
                    .compareTrueFirst(a.isExplicitChange(), b.isExplicitChange()) //
                    .compare(a.getType(), b.getType(), StringTools::safeComparisonNullLast) //
                    .compare(a.getOwner(), b.getOwner(), StringTools::safeComparisonNullLast) //
                    .result());

            // Ensure it is different
            updatedRole.setLinks(form.getLinks());
            updatedRole.setResources(form.getResources());
            if (StringTools.safeEquals(JsonTools.compactPrintWithoutNulls(existingRole), JsonTools.compactPrintWithoutNulls(updatedRole))) {
                result.getGlobalWarnings().add(translationService.translate("warning.nochange"));
            }

            if (!result.isSuccess() || !result.getGlobalWarnings().isEmpty()) {
                return;
            }

            // Edit
            updatedRole = roleRepository.save(updatedRole);
            auditingService.documentEdit(userId, existingRole, updatedRole);
        });

        return result;
    }

    @Override
    public RoleSmallWithPagination roleFindAll(String userId, int pageId, String search) {

        RoleSmallWithPagination results = new RoleSmallWithPagination();
        wrapExecution(results, () -> {

            // Permission
            entitlementService.isAdminOrFailUi(userId);

            // Parameters
            if (pageId < 1) {
                throw new UiException("error.pageStart1");
            }

            // List
            Page<Role> page;
            if (Strings.isNullOrEmpty(search)) {
                page = roleRepository.findAllNamesBy(PageRequest.of(pageId - 1, paginationService.getItemsPerPage(), Direction.ASC, "name"));
            } else {
                page = roleRepository.findAllNanesByNameLike(search, PageRequest.of(pageId - 1, paginationService.getItemsPerPage(), Direction.ASC, "name"));
            }

            paginationService.wrap(results, page, RoleSmall.class);

        });

        return results;
    }

    @Override
    public RoleResult roleFindOne(String userId, String roleName) {

        RoleResult result = new RoleResult();

        wrapExecution(result, () -> {

            // Permission
            entitlementService.isAdminOrFailUi(userId);

            // Get
            Optional<Role> role = roleRepository.findById(roleName);
            paginationService.wrap(result, role, com.foilen.infra.api.model.permission.Role.class);
        });

        return result;
    }

    @Override
    public UserApiNewFormResult userApiAdminCreate(String userId) {

        UserApiNewFormResult result = new UserApiNewFormResult();

        wrapExecution(result, () -> {

            // Permission
            entitlementService.isAdminOrFailUi(userId);

            if (!result.isSuccess()) {
                return;
            }

            // Generate id and password
            String userApiId = SecureRandomTools.randomHexString(25);
            String key = SecureRandomTools.randomHexString(25);
            result.setUserId(userApiId).setPassword(key);

            // Execute
            UserApi apiUser = new UserApi(userApiId, BCrypt.hashpw(key, BCrypt.gensalt(13)), "Admin");
            apiUser.setAdmin(true);
            userApiRepository.save(apiUser);
            auditingService.documentAdd(userId, apiUser);

        });

        return result;

    }

    @Override
    public FormResult userApiEdit(String userId, String userApiId, UserRoleEditForm form) {

        FormResult result = new FormResult();

        wrapExecution(result, () -> {

            // Permission
            entitlementService.isAdminOrFailUi(userId);

            // Get
            Optional<UserApi> existingUserApiO = userApiRepository.findById(userApiId);
            if (existingUserApiO.isEmpty()) {
                addValidationError(result, "userId", "error.notExists");
                return;
            }
            UserApi userApi = existingUserApiO.get();
            UserApi existingUserApi = JsonTools.clone(userApi);

            // Ensure it is different
            userApi.setRoles(form.getRoles());
            if (StringTools.safeEquals(JsonTools.compactPrintWithoutNulls(existingUserApi), JsonTools.compactPrintWithoutNulls(userApi))) {
                result.getGlobalWarnings().add(translationService.translate("warning.nochange"));
            }

            if (!result.isSuccess() || !result.getGlobalWarnings().isEmpty()) {
                return;
            }

            // Edit
            userApi = userApiRepository.save(userApi);
            auditingService.documentEdit(userId, existingUserApi, userApi);

        });

        return result;

    }

    @Override
    public UserApiWithPagination userApiFindAll(String userId, int pageId, String search) {

        UserApiWithPagination results = new UserApiWithPagination();
        wrapExecution(results, () -> {

            // Permission
            entitlementService.isAdminOrFailUi(userId);

            // Parameters
            if (pageId < 1) {
                throw new UiException("error.pageStart1");
            }

            // List
            Page<UserApi> page;
            if (Strings.isNullOrEmpty(search)) {
                page = userApiRepository.findAll(PageRequest.of(pageId - 1, paginationService.getItemsPerPage(), Direction.ASC, "userId"));
            } else {
                page = userApiRepository.findAllByUserIdLike(search, PageRequest.of(pageId - 1, paginationService.getItemsPerPage(), Direction.ASC, "userId"));
            }

            paginationService.wrap(results, page, com.foilen.infra.api.model.user.UserApi.class);

        });

        return results;
    }

    @Override
    public FormResult userHumanEdit(String userId, String userHumanId, UserRoleEditForm form) {

        FormResult result = new FormResult();

        wrapExecution(result, () -> {

            // Permission
            entitlementService.isAdminOrFailUi(userId);

            // Get
            Optional<UserHuman> existingUserHumanO = userHumanRepository.findById(userHumanId);
            if (existingUserHumanO.isEmpty()) {
                addValidationError(result, "userId", "error.notExists");
                return;
            }
            UserHuman userHuman = existingUserHumanO.get();
            UserHuman existingUserHuman = JsonTools.clone(userHuman);

            // Ensure it is different
            userHuman.setRoles(form.getRoles());
            if (StringTools.safeEquals(JsonTools.compactPrintWithoutNulls(existingUserHuman), JsonTools.compactPrintWithoutNulls(userHuman))) {
                result.getGlobalWarnings().add(translationService.translate("warning.nochange"));
            }

            if (!result.isSuccess() || !result.getGlobalWarnings().isEmpty()) {
                return;
            }

            // Edit
            userHuman = userHumanRepository.save(userHuman);
            auditingService.documentEdit(userId, existingUserHuman, userHuman);

        });

        return result;

    }

    @Override
    public UserHumanWithPagination userHumanFindAll(String userId, int pageId, String search) {

        UserHumanWithPagination results = new UserHumanWithPagination();
        wrapExecution(results, () -> {

            // Permission
            entitlementService.isAdminOrFailUi(userId);

            // Parameters
            if (pageId < 1) {
                throw new UiException("error.pageStart1");
            }

            // List
            Page<UserHuman> page;
            if (Strings.isNullOrEmpty(search)) {
                page = userHumanRepository.findAll(PageRequest.of(pageId - 1, paginationService.getItemsPerPage(), Direction.ASC, "userId"));
            } else {
                page = userHumanRepository.findAllByUserIdLikeOrEmailLike(search, search, PageRequest.of(pageId - 1, paginationService.getItemsPerPage(), Direction.ASC, "userId"));
            }

            paginationService.wrap(results, page, com.foilen.infra.api.model.user.UserHuman.class);

        });

        return results;
    }

}
