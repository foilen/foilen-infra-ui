/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.upgrades.mongodb.tmp;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.foilen.infra.plugin.core.system.mongodb.service.ResourceDefinitionService;
import com.foilen.infra.plugin.v1.core.resource.IPResourceDefinition;
import com.foilen.infra.plugin.v1.core.resource.IPResourceQuery;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.smalltools.tuple.Tuple2;
import com.foilen.smalltools.tuple.Tuple3;

public class TmpIPResourceServiceImpl implements IPResourceService {

    private ResourceDefinitionService resourceDefinitionService;

    public TmpIPResourceServiceImpl(ResourceDefinitionService resourceDefinitionService) {
        this.resourceDefinitionService = resourceDefinitionService;
    }

    @Override
    public <T extends IPResource> IPResourceQuery<T> createResourceQuery(Class<T> resourceClass) {
        throw new RuntimeException("Not mocked");
    }

    @Override
    public <T extends IPResource> IPResourceQuery<T> createResourceQuery(String resourceType) {
        throw new RuntimeException("Not mocked");
    }

    @Override
    public IPResourceDefinition getResourceDefinition(Class<? extends IPResource> resourceClass) {
        throw new RuntimeException("Not mocked");
    }

    @Override
    public IPResourceDefinition getResourceDefinition(IPResource resource) {
        throw new RuntimeException("Not mocked");
    }

    @Override
    public IPResourceDefinition getResourceDefinition(String resourceType) {
        throw new RuntimeException("Not mocked");
    }

    @Override
    public List<IPResourceDefinition> getResourceDefinitions() {
        return resourceDefinitionService.getResourceDefinitions();
    }

    @Override
    public boolean linkExistsByFromResourceAndLinkTypeAndToResource(IPResource fromResource, String linkType, IPResource toResource) {
        throw new RuntimeException("Not mocked");
    }

    @Override
    public List<Tuple2<String, ? extends IPResource>> linkFindAllByFromResource(IPResource fromResource) {
        throw new RuntimeException("Not mocked");
    }

    @Override
    public List<Tuple2<String, ? extends IPResource>> linkFindAllByFromResource(String fromResourceId) {
        throw new RuntimeException("Not mocked");
    }

    @Override
    public List<? extends IPResource> linkFindAllByFromResourceAndLinkType(IPResource fromResource, String linkType) {
        throw new RuntimeException("Not mocked");
    }

    @Override
    public <R extends IPResource> List<R> linkFindAllByFromResourceAndLinkTypeAndToResourceClass(IPResource fromResource, String linkType, Class<R> toResourceClass) {
        throw new RuntimeException("Not mocked");
    }

    @Override
    public <R extends IPResource> List<R> linkFindAllByFromResourceClassAndLinkTypeAndToResource(Class<R> fromResourceClass, String linkType, IPResource toResource) {
        throw new RuntimeException("Not mocked");
    }

    @Override
    public List<? extends IPResource> linkFindAllByLinkTypeAndToResource(String linkType, IPResource toResource) {
        throw new RuntimeException("Not mocked");
    }

    @Override
    public List<Tuple2<? extends IPResource, String>> linkFindAllByToResource(IPResource toResource) {

        throw new RuntimeException("Not mocked");
    }

    @Override
    public List<Tuple2<? extends IPResource, String>> linkFindAllByToResource(String toResourceId) {
        throw new RuntimeException("Not mocked");
    }

    @Override
    public List<Tuple3<IPResource, String, IPResource>> linkFindAllRelatedByResource(IPResource resource) {
        throw new RuntimeException("Not mocked");
    }

    @Override
    public List<Tuple3<IPResource, String, IPResource>> linkFindAllRelatedByResource(String internalResourceId) {
        throw new RuntimeException("Not mocked");
    }

    @Override
    public <R extends IPResource, T extends IPResource> boolean resourceEqualsPk(R a, T b) {
        throw new RuntimeException("Not mocked");
    }

    @Override
    public <T extends IPResource> Optional<T> resourceFind(IPResourceQuery<T> query) {
        throw new RuntimeException("Not mocked");
    }

    @Override
    public Optional<IPResource> resourceFind(String internalResourceId) {
        throw new RuntimeException("Not mocked");
    }

    @Override
    public <T extends IPResource> List<T> resourceFindAll(IPResourceQuery<T> query) {
        throw new RuntimeException("Not mocked");
    }

    @Override
    public <R extends IPResource> Optional<R> resourceFindByPk(R resource) {
        throw new RuntimeException("Not mocked");
    }

    @Override
    public Set<String> tagFindAllByResource(IPResource resource) {
        throw new RuntimeException("Not mocked");
    }

}
