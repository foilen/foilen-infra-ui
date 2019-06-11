/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services.hook;

import com.foilen.infra.plugin.core.system.common.changeexecution.hooks.ChangeExecutionHook;
import com.foilen.infra.plugin.v1.core.eventhandler.changes.ChangesInTransactionContext;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.infra.ui.services.AuditingService;
import com.foilen.smalltools.tools.AbstractBasics;

public class AuditingChangeExecutionHook extends AbstractBasics implements ChangeExecutionHook {

    private AuditingService auditingService;

    public AuditingChangeExecutionHook(AuditingService auditingService) {
        this.auditingService = auditingService;
    }

    @Override
    public void linkAdded(ChangesInTransactionContext changesInTransactionContext, IPResource fromResource, String linkType, IPResource toResource) {
        auditingService.linkAdd(changesInTransactionContext.getTxId(), changesInTransactionContext.isExplicitChange(), changesInTransactionContext.getUserType(),
                changesInTransactionContext.getUserName(), fromResource, linkType, toResource);
    }

    @Override
    public void linkDeleted(ChangesInTransactionContext changesInTransactionContext, IPResource fromResource, String linkType, IPResource toResource) {
        auditingService.linkDelete(changesInTransactionContext.getTxId(), changesInTransactionContext.isExplicitChange(), changesInTransactionContext.getUserType(),
                changesInTransactionContext.getUserName(), fromResource, linkType, toResource);
    }

    @Override
    public void resourceAdded(ChangesInTransactionContext changesInTransactionContext, IPResource resource) {
        auditingService.resourceAdd(changesInTransactionContext.getTxId(), changesInTransactionContext.isExplicitChange(), changesInTransactionContext.getUserType(),
                changesInTransactionContext.getUserName(), resource);
    }

    @Override
    public void resourceDeleted(ChangesInTransactionContext changesInTransactionContext, IPResource resource) {
        auditingService.resourceDelete(changesInTransactionContext.getTxId(), changesInTransactionContext.isExplicitChange(), changesInTransactionContext.getUserType(),
                changesInTransactionContext.getUserName(), resource);
    }

    @Override
    public void resourceUpdated(ChangesInTransactionContext changesInTransactionContext, IPResource previousResource, IPResource updatedResource) {
        auditingService.resourceUpdate(changesInTransactionContext.getTxId(), changesInTransactionContext.isExplicitChange(), changesInTransactionContext.getUserType(),
                changesInTransactionContext.getUserName(), previousResource, updatedResource);
    }

    @Override
    public void tagAdded(ChangesInTransactionContext changesInTransactionContext, IPResource resource, String tagName) {
        auditingService.tagAdd(changesInTransactionContext.getTxId(), changesInTransactionContext.isExplicitChange(), changesInTransactionContext.getUserType(),
                changesInTransactionContext.getUserName(), resource, tagName);
    }

    @Override
    public void tagDeleted(ChangesInTransactionContext changesInTransactionContext, IPResource resource, String tagName) {
        auditingService.tagDelete(changesInTransactionContext.getTxId(), changesInTransactionContext.isExplicitChange(), changesInTransactionContext.getUserType(),
                changesInTransactionContext.getUserName(), resource, tagName);
    }
}
