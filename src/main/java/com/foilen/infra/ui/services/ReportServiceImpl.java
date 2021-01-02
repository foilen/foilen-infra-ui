/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.foilen.infra.ui.repositories.ReportExecutionRepository;
import com.foilen.infra.ui.repositories.documents.ReportExecution;
import com.foilen.infra.ui.repositories.documents.models.ReportCount;
import com.foilen.infra.ui.repositories.documents.models.ReportTime;
import com.foilen.smalltools.tools.AbstractBasics;

@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class ReportServiceImpl extends AbstractBasics implements ReportService {

    @Autowired
    private ReportExecutionRepository reportExecutionRepository;

    @Override
    public void addReport(String txId, boolean success, List<ReportTime> reportTimes, List<ReportCount> reportCounts) {
        ReportExecution reportExecution = new ReportExecution(txId, success);

        reportExecution.setReportTimes(reportTimes);
        reportExecution.setReportCounts(reportCounts);

        reportExecutionRepository.save(reportExecution);

    }

}
