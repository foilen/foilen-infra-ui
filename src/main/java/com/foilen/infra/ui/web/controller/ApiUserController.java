/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.foilen.infra.ui.services.ApiUserService;
import com.foilen.infra.ui.services.EntitlementService;
import com.foilen.mvc.ui.UiSuccessErrorView;
import com.foilen.smalltools.tuple.Tuple2;

@Controller
@RequestMapping("apiUser")
public class ApiUserController {

    private static final String VIEW_BASE_PATH = "apiUser";

    @Autowired
    private ApiUserService apiUserService;
    @Autowired
    private EntitlementService entitlementService;

    @PostMapping("create")
    public ModelAndView create(Authentication authentication, RedirectAttributes redirectAttributes) {

        return new UiSuccessErrorView(redirectAttributes) //
                .setSuccessViewName("redirect:/" + VIEW_BASE_PATH + "/list") //
                .setErrorViewName("redirect:/" + VIEW_BASE_PATH + "/list") //
                .execute((ui, modelAndView) -> {
                    entitlementService.isAdminOrFailUi(authentication.getName());
                    Tuple2<String, String> userAndPassword = apiUserService.createAdminUser();
                    redirectAttributes.addFlashAttribute("createdUser", userAndPassword);
                });

    }

    @PostMapping("delete")
    public ModelAndView delete(Authentication authentication, @RequestParam("userId") String userId, RedirectAttributes redirectAttributes) {
        return new UiSuccessErrorView(redirectAttributes) //
                .setSuccessViewName("redirect:/" + VIEW_BASE_PATH + "/list") //
                .setErrorViewName("redirect:/" + VIEW_BASE_PATH + "/list") //
                .execute((ui, modelAndView) -> {
                    entitlementService.isAdminOrFailUi(authentication.getName());
                    apiUserService.deleteUser(userId);
                });
    }

    @GetMapping("list")
    public ModelAndView list(Authentication authentication) {

        entitlementService.isAdminOrFailUi(authentication.getName());

        ModelAndView modelAndView = new ModelAndView("apiUser/list");
        modelAndView.addObject("apiUsers", apiUserService.findAll());
        return modelAndView;
    }

}
