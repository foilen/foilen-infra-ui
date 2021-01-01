/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.web.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.foilen.infra.api.model.permission.OwnerRuleCreateOrEditForm;
import com.foilen.infra.api.model.permission.OwnerRuleResult;
import com.foilen.infra.api.model.permission.OwnerRuleWithPagination;
import com.foilen.infra.ui.services.ApiUserPermissionsService;
import com.foilen.smalltools.restapi.model.FormResult;

@RequestMapping(value = "api/ownerRule", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
@RestController
@SwaggerExpose
public class OwnerRuleApiController {

    @Autowired
    private ApiUserPermissionsService apiUserPermissionsService;

    @PostMapping("/")
    public FormResult ownerRuleAdd(Authentication authentication, @RequestBody OwnerRuleCreateOrEditForm form) {
        return apiUserPermissionsService.ownerRuleAdd(authentication.getName(), form);
    }

    @DeleteMapping("/{ownerRuleId}")
    public FormResult ownerRuleDelete(Authentication authentication, @PathVariable("ownerRuleId") String ownerRuleId) {
        return apiUserPermissionsService.ownerRuleDelete(authentication.getName(), ownerRuleId);
    }

    @PostMapping("/{ownerRuleId}")
    public FormResult ownerRuleEdit(Authentication authentication, @PathVariable("ownerRuleId") String ownerRuleId, @RequestBody OwnerRuleCreateOrEditForm form) {
        return apiUserPermissionsService.ownerRuleEdit(authentication.getName(), ownerRuleId, form);
    }

    @GetMapping("/")
    public OwnerRuleWithPagination ownerRuleFindAll(Authentication authentication, @RequestParam(required = false, defaultValue = "1") int pageId) {
        return apiUserPermissionsService.ownerRuleFindAll(authentication.getName(), pageId);
    }

    @GetMapping("/{ownerRuleId}")
    public OwnerRuleResult ownerRuleFindOne(Authentication authentication, @PathVariable("ownerRuleId") String ownerRuleId) {
        return apiUserPermissionsService.ownerRuleFindOne(authentication.getName(), ownerRuleId);
    }

}
