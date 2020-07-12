/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.repositories.documents;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import com.foilen.infra.api.model.permission.PermissionLink;
import com.foilen.infra.api.model.permission.PermissionResource;

@Document
public class Role {

    @Id
    private String name;
    @Version
    private long version;

    private List<PermissionResource> resources = new ArrayList<>();
    private List<PermissionLink> links = new ArrayList<>();

    public Role() {
    }

    public Role(String name) {
        this.name = name;
    }

    public List<PermissionLink> getLinks() {
        return links;
    }

    public String getName() {
        return name;
    }

    public List<PermissionResource> getResources() {
        return resources;
    }

    public Role setLinks(List<PermissionLink> links) {
        this.links = links;
        return this;
    }

    public Role setName(String name) {
        this.name = name;
        return this;
    }

    public Role setResources(List<PermissionResource> resources) {
        this.resources = resources;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Role [name=");
        builder.append(name);
        builder.append("]");
        return builder.toString();
    }

}
