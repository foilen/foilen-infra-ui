/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

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

import com.foilen.infra.api.model.permission.RoleCreateForm;
import com.foilen.infra.api.model.permission.RoleEditForm;
import com.foilen.infra.api.model.permission.RoleResult;
import com.foilen.infra.api.model.permission.RoleSmallWithPagination;
import com.foilen.infra.ui.services.ApiUserPermissionsService;
import com.foilen.smalltools.restapi.model.FormResult;

@RequestMapping(value = "api/role", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
@RestController
@SwaggerExpose
public class RoleApiController {

    @Autowired
    private ApiUserPermissionsService apiUserPermissionsService;

    @PostMapping("/")
    public FormResult roleAdd(Authentication authentication, @RequestBody RoleCreateForm form) {
        return apiUserPermissionsService.roleAdd(authentication.getName(), form);
    }

    @DeleteMapping("/{roleName}")
    public FormResult roleDelete(Authentication authentication, @PathVariable("roleName") String roleName) {
        return apiUserPermissionsService.roleDelete(authentication.getName(), roleName);
    }

    @PostMapping("/{roleName}")
    public FormResult roleEdit(Authentication authentication, @PathVariable("roleName") String roleName, @RequestBody RoleEditForm form) {
        return apiUserPermissionsService.roleEdit(authentication.getName(), roleName, form);
    }

    @GetMapping("/")
    public RoleSmallWithPagination roleFindAll(Authentication authentication, @RequestParam(required = false, defaultValue = "1") int pageId, @RequestParam(required = false) String search) {
        return apiUserPermissionsService.roleFindAll(authentication.getName(), pageId, search);
    }

    @GetMapping("/{roleName}")
    public RoleResult roleFindOne(Authentication authentication, @PathVariable("roleName") String roleName) {
        return apiUserPermissionsService.roleFindOne(authentication.getName(), roleName);
    }

}
