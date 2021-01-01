/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.web.interceptor;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.foilen.infra.ui.repositories.UserHumanRepository;
import com.foilen.infra.ui.repositories.documents.UserHuman;
import com.foilen.login.spring.client.security.FoilenLoginUserDetails;
import com.foilen.login.spring.services.FoilenLoginService;
import com.foilen.smalltools.tools.StringTools;

/**
 * Add the user in the database if missing and update the email.
 */
public class AddUserInDatabaseInterceptor extends AbstractCommonHandlerInterceptor {

    @Autowired
    private FoilenLoginService foilenLoginService;
    @Autowired
    private UserHumanRepository userHumanRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // Only if the type is FoilenLoginUserDetails
        SecurityContext context = SecurityContextHolder.getContext();
        if (context == null) {
            return true;
        }
        Authentication authentication = context.getAuthentication();
        if (authentication == null) {
            return true;
        }
        if (!(authentication.getPrincipal() instanceof FoilenLoginUserDetails)) {
            return true;
        }

        // Logged in user
        FoilenLoginUserDetails userDetails = foilenLoginService.getLoggedInUserDetails();
        if (userDetails != null) {

            Optional<UserHuman> userO = userHumanRepository.findById(userDetails.getUsername());

            if (userO.isPresent()) {
                UserHuman user = userO.get();
                if (!StringTools.safeEquals(userDetails.getEmail(), user.getEmail())) {
                    user.setEmail(userDetails.getEmail());
                    userHumanRepository.save(user);
                }
            } else {
                UserHuman user = new UserHuman();
                user.setUserId(userDetails.getUsername());
                user.setEmail(userDetails.getEmail());

                userHumanRepository.save(user);
            }
        }

        return true;
    }

}
