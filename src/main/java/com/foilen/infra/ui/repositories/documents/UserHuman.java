/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.repositories.documents;

import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.Authentication;

import com.foilen.infra.plugin.v1.core.eventhandler.changes.AuditUserType;
import com.foilen.login.spring.client.security.FoilenAuthentication;
import com.foilen.login.spring.client.security.FoilenLoginUserDetails;

@Document
public class UserHuman extends AbstractUser {

    @Version
    private long version;

    private String email;

    public UserHuman() {
    }

    public UserHuman(String userId, boolean isAdmin) {
        super(userId, isAdmin);
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getUserHashedKey() {
        return null;
    }

    public UserHuman setEmail(String email) {
        this.email = email;
        return this;
    }

    @Override
    public Authentication toAuthentication() {
        return new FoilenAuthentication(new FoilenLoginUserDetails(getUserId(), email));
    }

    @Override
    public AuditUserType toUserType() {
        return AuditUserType.USER;
    }

}
