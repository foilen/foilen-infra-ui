/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.repositories.documents;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class OwnerRule {

    @Id
    private String id;
    @Version
    private long version;

    private String resourceNameStartsWith;
    private String resourceNameEndsWith;

    private String assignOwner;

    public String getAssignOwner() {
        return assignOwner;
    }

    public String getId() {
        return id;
    }

    public String getResourceNameEndsWith() {
        return resourceNameEndsWith;
    }

    public String getResourceNameStartsWith() {
        return resourceNameStartsWith;
    }

    public OwnerRule setAssignOwner(String assignOwner) {
        this.assignOwner = assignOwner;
        return this;
    }

    public OwnerRule setId(String id) {
        this.id = id;
        return this;
    }

    public OwnerRule setResourceNameEndsWith(String resourceNameEndsWith) {
        this.resourceNameEndsWith = resourceNameEndsWith;
        return this;
    }

    public OwnerRule setResourceNameStartsWith(String resourceNameStartsWith) {
        this.resourceNameStartsWith = resourceNameStartsWith;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("OwnerRule [resourceNameStartsWith=");
        builder.append(resourceNameStartsWith);
        builder.append(", resourceNameEndsWith=");
        builder.append(resourceNameEndsWith);
        builder.append(", assignOwner=");
        builder.append(assignOwner);
        builder.append("]");
        return builder.toString();
    }

}
