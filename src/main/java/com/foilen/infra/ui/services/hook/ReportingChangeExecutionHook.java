/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services.hook;

import java.util.stream.Collectors;

import com.foilen.infra.plugin.v1.core.eventhandler.changes.ChangeExecutionHook;
import com.foilen.infra.plugin.v1.core.eventhandler.changes.ChangesInTransactionContext;
import com.foilen.infra.ui.repositories.documents.models.ReportCount;
import com.foilen.infra.ui.repositories.documents.models.ReportTime;
import com.foilen.infra.ui.services.ReportService;
import com.foilen.smalltools.tools.AbstractBasics;

public class ReportingChangeExecutionHook extends AbstractBasics implements ChangeExecutionHook {

    private ReportService reportService;

    public ReportingChangeExecutionHook(ReportService reportService) {
        this.reportService = reportService;
    }

    @Override
    public void failureInfinite(ChangesInTransactionContext changesInTransactionContext) {
        reportService.addReport(changesInTransactionContext.getTxId(), false, //
                changesInTransactionContext.getExecutionTimeInMsByActionHandler().entrySet().stream().map(entry -> new ReportTime(entry.getKey(), entry.getValue().get())).collect(Collectors.toList()), //
                changesInTransactionContext.getUpdateCountByResourceId().entrySet().stream().map(entry -> new ReportCount(entry.getKey(), entry.getValue().get())).collect(Collectors.toList()));
    }

    @Override
    public void success(ChangesInTransactionContext changesInTransactionContext) {
        reportService.addReport(changesInTransactionContext.getTxId(), true, //
                changesInTransactionContext.getExecutionTimeInMsByActionHandler().entrySet().stream().map(entry -> new ReportTime(entry.getKey(), entry.getValue().get())).collect(Collectors.toList()), //
                changesInTransactionContext.getUpdateCountByResourceId().entrySet().stream().map(entry -> new ReportCount(entry.getKey(), entry.getValue().get())).collect(Collectors.toList()));
    }
}
