/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import com.foilen.infra.api.model.permission.OwnerRuleCreateOrEditForm;
import com.foilen.infra.api.model.permission.OwnerRuleResult;
import com.foilen.infra.api.model.permission.OwnerRuleWithPagination;
import com.foilen.infra.api.model.permission.RoleCreateForm;
import com.foilen.infra.api.model.permission.RoleEditForm;
import com.foilen.infra.api.model.permission.RoleResult;
import com.foilen.infra.api.model.permission.RoleSmallWithPagination;
import com.foilen.infra.api.model.user.UserApiNewFormResult;
import com.foilen.infra.api.model.user.UserApiWithPagination;
import com.foilen.infra.api.model.user.UserHumanWithPagination;
import com.foilen.infra.api.model.user.UserRoleEditForm;
import com.foilen.smalltools.restapi.model.FormResult;

public interface ApiUserPermissionsService {

    FormResult ownerRuleAdd(String userId, OwnerRuleCreateOrEditForm form);

    FormResult ownerRuleDelete(String userId, String ownerRuleId);

    FormResult ownerRuleEdit(String userId, String ownerRuleId, OwnerRuleCreateOrEditForm form);

    OwnerRuleWithPagination ownerRuleFindAll(String userId, int pageId);

    OwnerRuleResult ownerRuleFindOne(String userId, String ownerRuleId);

    FormResult roleAdd(String userId, RoleCreateForm form);

    FormResult roleDelete(String userId, String roleName);

    FormResult roleEdit(String userId, String roleName, RoleEditForm form);

    RoleSmallWithPagination roleFindAll(String userId, int pageId, String search);

    RoleResult roleFindOne(String userId, String roleName);

    UserApiNewFormResult userApiAdminCreate(String userId);

    FormResult userApiEdit(String userId, String userApiId, UserRoleEditForm form);

    UserApiWithPagination userApiFindAll(String userId, int pageId, String search);

    FormResult userHumanCreateByEmail(String userId, String userEmail);

    FormResult userHumanEdit(String userId, String userHumanId, UserRoleEditForm form);

    UserHumanWithPagination userHumanFindAll(String userId, int pageId, String search);

}
