/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services.hook;

import com.foilen.infra.plugin.core.system.common.changeexecution.ApplyChangesContext;
import com.foilen.infra.plugin.core.system.common.changeexecution.hooks.ChangeExecutionHook;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.infra.ui.services.AuditingService;
import com.foilen.smalltools.tools.AbstractBasics;

public class AuditingChangeExecutionHook extends AbstractBasics implements ChangeExecutionHook {

    private AuditingService auditingService;

    public AuditingChangeExecutionHook(AuditingService auditingService) {
        this.auditingService = auditingService;
    }

    @Override
    public void linkAdded(ApplyChangesContext applyChangesContext, IPResource fromResource, String linkType, IPResource toResource) {
        auditingService.linkAdd(applyChangesContext.getTxId(), applyChangesContext.isExplicitChange(), applyChangesContext.getUserType(), applyChangesContext.getUserName(), fromResource, linkType,
                toResource);
    }

    @Override
    public void linkDeleted(ApplyChangesContext applyChangesContext, IPResource fromResource, String linkType, IPResource toResource) {
        auditingService.linkDelete(applyChangesContext.getTxId(), applyChangesContext.isExplicitChange(), applyChangesContext.getUserType(), applyChangesContext.getUserName(), fromResource, linkType,
                toResource);
    }

    @Override
    public void resourceAdded(ApplyChangesContext applyChangesContext, IPResource resource) {
        auditingService.resourceAdd(applyChangesContext.getTxId(), applyChangesContext.isExplicitChange(), applyChangesContext.getUserType(), applyChangesContext.getUserName(), resource);
    }

    @Override
    public void resourceDeleted(ApplyChangesContext applyChangesContext, IPResource resource) {
        auditingService.resourceDelete(applyChangesContext.getTxId(), applyChangesContext.isExplicitChange(), applyChangesContext.getUserType(), applyChangesContext.getUserName(), resource);
    }

    @Override
    public void resourceUpdated(ApplyChangesContext applyChangesContext, IPResource previousResource, IPResource updatedResource) {
        auditingService.resourceUpdate(applyChangesContext.getTxId(), applyChangesContext.isExplicitChange(), applyChangesContext.getUserType(), applyChangesContext.getUserName(), previousResource,
                updatedResource);
    }

    @Override
    public void tagAdded(ApplyChangesContext applyChangesContext, IPResource resource, String tagName) {
        auditingService.tagAdd(applyChangesContext.getTxId(), applyChangesContext.isExplicitChange(), applyChangesContext.getUserType(), applyChangesContext.getUserName(), resource, tagName);
    }

    @Override
    public void tagDeleted(ApplyChangesContext applyChangesContext, IPResource resource, String tagName) {
        auditingService.tagDelete(applyChangesContext.getTxId(), applyChangesContext.isExplicitChange(), applyChangesContext.getUserType(), applyChangesContext.getUserName(), resource, tagName);
    }
}
