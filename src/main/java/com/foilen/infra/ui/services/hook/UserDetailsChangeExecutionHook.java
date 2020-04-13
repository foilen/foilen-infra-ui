/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services.hook;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.foilen.infra.plugin.v1.core.eventhandler.changes.AuditUserType;
import com.foilen.infra.plugin.v1.core.eventhandler.changes.ChangeExecutionHook;
import com.foilen.infra.plugin.v1.core.eventhandler.changes.ChangesInTransactionContext;
import com.foilen.login.spring.client.security.FoilenAuthentication;
import com.foilen.smalltools.tools.AbstractBasics;

public class UserDetailsChangeExecutionHook extends AbstractBasics implements ChangeExecutionHook {

    @Override
    public void fillApplyChangesContext(ChangesInTransactionContext changesInTransactionContext) {
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
        changesInTransactionContext.setUserType(userType);
        changesInTransactionContext.setUserName(userName);
    }
}
