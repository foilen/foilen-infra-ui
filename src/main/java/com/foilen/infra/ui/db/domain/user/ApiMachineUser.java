/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.db.domain.user;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class ApiMachineUser extends ApiUser implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    @Column(nullable = false)
    private String machineName;
    @Column(nullable = false, length = 250)
    private String userKey;

    public ApiMachineUser() {
    }

    public ApiMachineUser(String userId, String userKey, String userHashedKey, String machineName, Date expireOn) {
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

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

}
