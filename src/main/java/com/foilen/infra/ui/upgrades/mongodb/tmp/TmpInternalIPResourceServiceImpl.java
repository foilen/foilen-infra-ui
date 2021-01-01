/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.upgrades.mongodb.tmp;

import java.util.List;

import com.foilen.infra.plugin.core.system.mongodb.service.ResourceDefinitionService;
import com.foilen.infra.plugin.v1.core.resource.IPResourceDefinition;
import com.foilen.infra.plugin.v1.core.service.internal.InternalIPResourceService;
import com.foilen.infra.plugin.v1.model.resource.IPResource;

public class TmpInternalIPResourceServiceImpl implements InternalIPResourceService {

    private ResourceDefinitionService resourceDefinitionService;

    public TmpInternalIPResourceServiceImpl(ResourceDefinitionService resourceDefinitionService) {
        this.resourceDefinitionService = resourceDefinitionService;
    }

    @Override
    public void resourceAdd(IPResourceDefinition resourceDefinition) {
        resourceDefinitionService.resourceAdd(resourceDefinition);
    }

    @Override
    public List<? extends IPResource> resourceFindAll() {
        throw new RuntimeException("Not mocked");
    }

}
