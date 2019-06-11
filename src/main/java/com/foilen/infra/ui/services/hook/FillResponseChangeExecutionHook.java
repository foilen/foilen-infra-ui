/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services.hook;

import java.util.Map;

import org.springframework.data.domain.Page;

import com.foilen.infra.api.model.AuditAction;
import com.foilen.infra.api.model.AuditItemSmall;
import com.foilen.infra.api.model.AuditType;
import com.foilen.infra.api.model.ResourceDetailsSmall;
import com.foilen.infra.api.response.ResponseResourceAppliedChanges;
import com.foilen.infra.plugin.core.system.common.changeexecution.hooks.ChangeExecutionHook;
import com.foilen.infra.plugin.v1.core.eventhandler.changes.ChangesInTransactionContext;
import com.foilen.infra.ui.db.domain.audit.AuditItem;
import com.foilen.infra.ui.services.AuditingService;
import com.foilen.smalltools.restapi.model.ApiPagination;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.JsonTools;

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

    @SuppressWarnings("unchecked")
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

            String resourceFirstJson = it.getResourceFirst();
            if (resourceFirstJson != null && it.getResourceFirstType() != null) {
                Map<String, Object> resourceFirst = JsonTools.readFromString(resourceFirstJson, Map.class);
                auditItem.setResourceFirst(new ResourceDetailsSmall(it.getResourceFirstType(), (String) resourceFirst.get("resourceName")));
            }
            String resourceSecondJson = it.getResourceSecond();
            if (resourceSecondJson != null && it.getResourceSecondType() != null) {
                Map<String, Object> resourceSecond = JsonTools.readFromString(resourceSecondJson, Map.class);
                auditItem.setResourceSecond(new ResourceDetailsSmall(it.getResourceSecondType(), (String) resourceSecond.get("resourceName")));
            }

            auditItem.setLinkType(it.getLinkType());

            auditItem.setTagName(it.getTagName());

            responseResourceAppliedChanges.getAuditItems().getItems().add(auditItem);
        });
        responseResourceAppliedChanges.getAuditItems().setPagination(new ApiPagination(auditPage));
    }

    @Override
    public void success(ChangesInTransactionContext changesInTransactionContext) {
        fill(changesInTransactionContext);
    }

}
