/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services.hook;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.foilen.infra.plugin.core.system.common.changeexecution.ApplyChangesContext;
import com.foilen.infra.plugin.core.system.common.changeexecution.AuditUserType;
import com.foilen.infra.plugin.core.system.common.changeexecution.hooks.ChangeExecutionHook;
import com.foilen.login.spring.client.security.FoilenAuthentication;
import com.foilen.smalltools.tools.AbstractBasics;

public class UserDetailsChangeExecutionHook extends AbstractBasics implements ChangeExecutionHook {

    @Override
    public void fillApplyChangesContext(ApplyChangesContext applyChangesContext) {
        AuditUserType userType;
        String userName;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            userType = AuditUserType.SYSTEM;
            userName = null;
        } else if (auth instanceof FoilenAuthentication) {
            userType = AuditUserType.USER;
            userName = auth.getName();
        } else {
            userType = AuditUserType.API;
            userName = auth.getName();
        }
        applyChangesContext.setUserType(userType);
        applyChangesContext.setUserName(userName);
    }
}
