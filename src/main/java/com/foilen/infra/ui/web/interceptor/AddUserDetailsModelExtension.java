/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.web.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;

import com.foilen.infra.ui.db.dao.UserDao;
import com.foilen.infra.ui.db.domain.user.User;
import com.foilen.login.spring.client.security.FoilenLoginUserDetails;
import com.foilen.login.spring.services.FoilenLoginService;

/**
 * Add all the details about the currently logged in user.
 */
public class AddUserDetailsModelExtension extends AbstractCommonHandlerInterceptor {

    @Autowired
    private FoilenLoginService foilenLoginService;
    @Autowired
    private UserDao userDao;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        if (modelAndView != null && !isRedirect(modelAndView)) {

            // Logged in user
            FoilenLoginUserDetails userDetails = foilenLoginService.getLoggedInUserDetails();
            if (userDetails != null) {
                modelAndView.addObject("userDetails", userDetails);
                User user = userDao.findByUserId(userDetails.getUsername());
                if (user == null) {
                    modelAndView.addObject("isAdmin", false);
                } else {
                    modelAndView.addObject("isAdmin", user.isAdmin());
                }
            }

        }

    }

}
