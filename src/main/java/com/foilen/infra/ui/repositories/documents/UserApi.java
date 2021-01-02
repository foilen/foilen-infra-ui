/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.repositories.documents;

import java.util.Date;

import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import com.foilen.smalltools.tools.DateTools;

@Document
public class UserApi extends AbstractUser {

    @Version
    private long version;

    private String userHashedKey;
    private String description;

    private Date createdOn = new Date();
    private Date expireOn;

    public UserApi() {
    }

    public UserApi(String userId, String userHashedKey, String description) {
        super(userId);
        this.userHashedKey = userHashedKey;
        this.description = description;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public String getCreatedOnText() {
        return DateTools.formatFull(createdOn);
    }

    public String getDescription() {
        return description;
    }

    public Date getExpireOn() {
        return expireOn;
    }

    public String getExpireOnText() {
        return DateTools.formatFull(expireOn);
    }

    @Override
    public String getUserHashedKey() {
        return userHashedKey;
    }

    public UserApi setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
        return this;
    }

    public UserApi setDescription(String description) {
        this.description = description;
        return this;
    }

    public UserApi setExpireOn(Date expireOn) {
        this.expireOn = expireOn;
        return this;
    }

    public UserApi setUserHashedKey(String userHashedKey) {
        this.userHashedKey = userHashedKey;
        return this;
    }

}
