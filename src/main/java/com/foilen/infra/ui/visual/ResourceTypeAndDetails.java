/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.visual;

import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.smalltools.tools.AbstractBasics;

public class ResourceTypeAndDetails extends AbstractBasics {

    private String type;
    private IPResource resource;

    public ResourceTypeAndDetails() {
    }

    public ResourceTypeAndDetails(String type, IPResource resource) {
        this.type = type;
        this.resource = resource;
    }

    public IPResource getResource() {
        return resource;
    }

    public String getType() {
        return type;
    }

    public void setResource(IPResource resource) {
        this.resource = resource;
    }

    public void setType(String type) {
        this.type = type;
    }

}
