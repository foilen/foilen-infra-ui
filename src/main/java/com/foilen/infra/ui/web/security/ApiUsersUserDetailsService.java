/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.web.security;

import java.util.Collections;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.foilen.infra.ui.repositories.documents.AbstractUser;
import com.foilen.infra.ui.services.EntitlementService;
import com.foilen.smalltools.tools.AbstractBasics;

public class ApiUsersUserDetailsService extends AbstractBasics implements UserDetailsService {

    private EntitlementService entitlementService;

    public ApiUsersUserDetailsService(EntitlementService entitlementService) {
        this.entitlementService = entitlementService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        AbstractUser user = entitlementService.getUser(username);
        if (user == null) {
            throw new UsernameNotFoundException("Not found");
        }

        return new User(username, user.getUserHashedKey(), Collections.emptyList());

    }

}
