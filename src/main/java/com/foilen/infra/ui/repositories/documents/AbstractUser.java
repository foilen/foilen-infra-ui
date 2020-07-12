/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.repositories.documents;

import java.util.SortedSet;
import java.util.TreeSet;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AbstractUser {

    @Id
    private String userId;
    private boolean isAdmin;

    private SortedSet<String> roles = new TreeSet<>();

    public AbstractUser() {
    }

    public AbstractUser(String userId) {
        this.userId = userId;
    }

    public AbstractUser(String userId, boolean isAdmin) {
        this.userId = userId;
        this.isAdmin = isAdmin;
    }

    public void addRole(String... roles) {
        for (String role : roles) {
            this.roles.add(role);
        }
    }

    public SortedSet<String> getRoles() {
        return roles;
    }

    public abstract String getUserHashedKey();

    public String getUserId() {
        return this.userId;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public void setRoles(SortedSet<String> roles) {
        this.roles = roles;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

}