/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.repositories.documents;

import java.util.Date;

import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class UserApiMachine extends UserApi {

    private String machineName;
    private String userKey;

    public UserApiMachine() {
    }

    public UserApiMachine(String userId, String userKey, String userHashedKey, String machineName, Date expireOn) {
        super(userId, userHashedKey, "For machine " + machineName);
        this.machineName = machineName;
        this.userKey = userKey;
        setExpireOn(expireOn);
    }

    public String getMachineName() {
        return machineName;
    }

    public String getUserKey() {
        return userKey;
    }

    public UserApiMachine setMachineName(String machineName) {
        this.machineName = machineName;
        return this;
    }

    public UserApiMachine setUserKey(String userKey) {
        this.userKey = userKey;
        return this;
    }

}
