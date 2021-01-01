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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.foilen.infra.api.model.user.UserApiNewFormResult;
import com.foilen.infra.api.model.user.UserApiWithPagination;
import com.foilen.infra.api.model.user.UserRoleEditForm;
import com.foilen.infra.ui.services.ApiUserPermissionsService;
import com.foilen.smalltools.restapi.model.FormResult;

@RequestMapping(value = "api/userApi", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
@RestController
@SwaggerExpose
public class UserApiApiController {

    @Autowired
    private ApiUserPermissionsService apiUserPermissionsService;

    @PostMapping("/admin")
    public UserApiNewFormResult userApiAdminCreate(Authentication authentication) {
        return apiUserPermissionsService.userApiAdminCreate(authentication.getName());
    }

    @PostMapping("/{userId}/roles")
    public FormResult userApiEdit(Authentication authentication, @PathVariable("userId") String userId, @RequestBody UserRoleEditForm form) {
        return apiUserPermissionsService.userApiEdit(authentication.getName(), userId, form);
    }

    @GetMapping("/")
    public UserApiWithPagination userApiFindAll(Authentication authentication, @RequestParam(required = false, defaultValue = "1") int pageId, @RequestParam(required = false) String search) {
        return apiUserPermissionsService.userApiFindAll(authentication.getName(), pageId, search);
    }

}
