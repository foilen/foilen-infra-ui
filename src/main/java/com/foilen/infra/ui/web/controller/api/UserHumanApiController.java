/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

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

import com.foilen.infra.api.model.user.UserHumanWithPagination;
import com.foilen.infra.api.model.user.UserRoleEditForm;
import com.foilen.infra.ui.services.ApiUserPermissionsService;
import com.foilen.smalltools.restapi.model.FormResult;

@RequestMapping(value = "api/userHuman", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
@RestController
@SwaggerExpose
public class UserHumanApiController {

    @Autowired
    private ApiUserPermissionsService apiUserPermissionsService;

    @PostMapping("/createByEmail/{userEmail}")
    public FormResult userHumanCreateByEmail(Authentication authentication, @PathVariable("userEmail") String userEmail) {
        return apiUserPermissionsService.userHumanCreateByEmail(authentication.getName(), userEmail);
    }

    @PostMapping("/{userId}/roles")
    public FormResult userHumanEdit(Authentication authentication, @PathVariable("userId") String userId, @RequestBody UserRoleEditForm form) {
        return apiUserPermissionsService.userHumanEdit(authentication.getName(), userId, form);
    }

    @GetMapping("/")
    public UserHumanWithPagination userHumanFindAll(Authentication authentication, @RequestParam(required = false, defaultValue = "1") int pageId, @RequestParam(required = false) String search) {
        return apiUserPermissionsService.userHumanFindAll(authentication.getName(), pageId, search);
    }

}
