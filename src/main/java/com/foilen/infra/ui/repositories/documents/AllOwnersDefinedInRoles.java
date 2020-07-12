/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.repositories.documents;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * The view of all the possible owners.
 */
@Document
public class AllOwnersDefinedInRoles {

    @Id
    private String owner;

    public String getOwner() {
        return owner;
    }

    public AllOwnersDefinedInRoles setOwner(String owner) {
        this.owner = owner;
        return this;
    }

}
