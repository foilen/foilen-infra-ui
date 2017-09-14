/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
 package com.foilen.infra.ui.db.domain.user;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Version;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class ApiUser implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    @Version
    private long version;

    @Column(unique = true, nullable = false, length = 25)
    private String userId;
    @Column(nullable = false, length = 250)
    private String userHashedKey;
    @Column(nullable = false)
    private String description;

    @Column(name = "isAdmin", nullable = false)
    private boolean admin;

    private Date createdOn = new Date();
    private Date expireOn;

    public ApiUser() {
    }

    public ApiUser(String userId, String userHashedKey, String description) {
        this.userId = userId;
        this.userHashedKey = userHashedKey;
        this.description = description;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public String getDescription() {
        return description;
    }

    public Date getExpireOn() {
        return expireOn;
    }

    public String getUserHashedKey() {
        return userHashedKey;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setExpireOn(Date expireOn) {
        this.expireOn = expireOn;
    }

    public void setUserHashedKey(String userHashedKey) {
        this.userHashedKey = userHashedKey;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

}
