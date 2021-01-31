/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services.hook;

import org.springframework.data.domain.Page;

import com.foilen.infra.api.model.audit.AuditAction;
import com.foilen.infra.api.model.audit.AuditItemSmall;
import com.foilen.infra.api.model.audit.AuditType;
import com.foilen.infra.api.model.resource.ResourceDetailsSmall;
import com.foilen.infra.api.response.ResponseResourceAppliedChanges;
import com.foilen.infra.plugin.v1.core.eventhandler.changes.ChangeExecutionHook;
import com.foilen.infra.plugin.v1.core.eventhandler.changes.ChangesInTransactionContext;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.infra.ui.repositories.documents.AuditItem;
import com.foilen.infra.ui.services.AuditingService;
import com.foilen.smalltools.restapi.model.ApiPagination;
import com.foilen.smalltools.tools.AbstractBasics;

public class FillResponseChangeExecutionHook extends AbstractBasics implements ChangeExecutionHook {

    private AuditingService auditingService;

    private ResponseResourceAppliedChanges responseResourceAppliedChanges;

    public FillResponseChangeExecutionHook(AuditingService auditingService, ResponseResourceAppliedChanges responseResourceAppliedChanges) {
        this.auditingService = auditingService;
        this.responseResourceAppliedChanges = responseResourceAppliedChanges;
    }

    @Override
    public void failureInfinite(ChangesInTransactionContext changesInTransactionContext) {
        fill(changesInTransactionContext);
    }

    private void fill(ChangesInTransactionContext changesInTransactionContext) {

        responseResourceAppliedChanges.setTxId(changesInTransactionContext.getTxId());

        changesInTransactionContext.getUpdateCountByResourceId().forEach((k, v) -> {
            responseResourceAppliedChanges.getUpdateCountByResourceId().put(k, v.get());
        });
        changesInTransactionContext.getExecutionTimeInMsByActionHandler().forEach((k, v) -> {
            responseResourceAppliedChanges.getExecutionTimeInMsByActionHandler().put(k, v.get());
        });

        Page<AuditItem> auditPage = auditingService.findAllByTxId(changesInTransactionContext.getTxId(), 0, 100);
        auditPage.forEach(it -> {

            AuditItemSmall auditItem = new AuditItemSmall();
            auditItem.setType(AuditType.valueOf(it.getType()));
            auditItem.setAction(AuditAction.valueOf(it.getAction()));

            if (it.getResourceFirst() != null) {
                auditItem.setResourceFirst(new ResourceDetailsSmall(getResourceId(it.getResourceFirst()), it.getResourceFirstType(), getResourceName(it.getResourceFirst())));
            }
            if (it.getResourceSecond() != null) {
                auditItem.setResourceSecond(new ResourceDetailsSmall(getResourceId(it.getResourceSecond()), it.getResourceSecondType(), getResourceName(it.getResourceSecond())));
            }

            auditItem.setLinkType(it.getLinkType());

            auditItem.setTagName(it.getTagName());

            responseResourceAppliedChanges.getAuditItems().getItems().add(auditItem);
        });
        responseResourceAppliedChanges.getAuditItems().setPagination(new ApiPagination(auditPage));
    }

    private String getResourceId(Object resource) {
        String resourceId = "N/A";
        if (resource instanceof IPResource) {
            resourceId = ((IPResource) resource).getInternalId();
        }
        return resourceId;
    }

    private String getResourceName(Object resource) {
        String resourceName = "N/A";
        if (resource instanceof IPResource) {
            resourceName = ((IPResource) resource).getResourceName();
        }
        return resourceName;
    }

    @Override
    public void success(ChangesInTransactionContext changesInTransactionContext) {
        fill(changesInTransactionContext);
    }

}
