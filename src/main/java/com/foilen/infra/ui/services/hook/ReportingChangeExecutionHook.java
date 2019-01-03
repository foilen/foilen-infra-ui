/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services.hook;

import java.util.stream.Collectors;

import com.foilen.infra.plugin.core.system.common.changeexecution.ApplyChangesContext;
import com.foilen.infra.plugin.core.system.common.changeexecution.hooks.ChangeExecutionHook;
import com.foilen.infra.ui.db.domain.reporting.ReportCount;
import com.foilen.infra.ui.db.domain.reporting.ReportTime;
import com.foilen.infra.ui.services.ReportService;
import com.foilen.smalltools.tools.AbstractBasics;

public class ReportingChangeExecutionHook extends AbstractBasics implements ChangeExecutionHook {

    private ReportService reportService;

    public ReportingChangeExecutionHook(ReportService reportService) {
        this.reportService = reportService;
    }

    @Override
    public void failureInfinite(ApplyChangesContext applyChangesContext) {
        reportService.addReport(applyChangesContext.getTxId(), false, //
                applyChangesContext.getExecutionTimeInMsByUpdateHandler().entrySet().stream().map(entry -> new ReportTime(entry.getKey(), entry.getValue().get())).collect(Collectors.toList()), //
                applyChangesContext.getUpdateCountByResourceId().entrySet().stream().map(entry -> new ReportCount(entry.getKey(), entry.getValue().get())).collect(Collectors.toList()));
    }

    @Override
    public void success(ApplyChangesContext applyChangesContext) {
        reportService.addReport(applyChangesContext.getTxId(), true, //
                applyChangesContext.getExecutionTimeInMsByUpdateHandler().entrySet().stream().map(entry -> new ReportTime(entry.getKey(), entry.getValue().get())).collect(Collectors.toList()), //
                applyChangesContext.getUpdateCountByResourceId().entrySet().stream().map(entry -> new ReportCount(entry.getKey(), entry.getValue().get())).collect(Collectors.toList()));
    }
}
