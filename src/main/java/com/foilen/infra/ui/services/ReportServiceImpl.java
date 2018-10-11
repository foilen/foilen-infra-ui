/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.foilen.infra.ui.db.dao.ReportCountDao;
import com.foilen.infra.ui.db.dao.ReportExecutionDao;
import com.foilen.infra.ui.db.dao.ReportTimeDao;
import com.foilen.infra.ui.db.domain.reporting.ReportCount;
import com.foilen.infra.ui.db.domain.reporting.ReportExecution;
import com.foilen.infra.ui.db.domain.reporting.ReportTime;
import com.foilen.smalltools.tools.AbstractBasics;

@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class ReportServiceImpl extends AbstractBasics implements ReportService {

    @Autowired
    private ReportCountDao reportCountDao;
    @Autowired
    private ReportExecutionDao reportExecutionDao;
    @Autowired
    private ReportTimeDao reportTimeDao;

    @Override
    public void addReport(String txId, boolean success, List<ReportTime> reportTimes, List<ReportCount> reportCounts) {
        ReportExecution reportExecution = new ReportExecution(txId, success);
        reportExecution = reportExecutionDao.save(reportExecution);
        ReportExecution finalReportExecution = reportExecution;

        reportTimes.forEach(it -> it.setReportExecution(finalReportExecution));
        reportTimeDao.save(reportTimes);
        reportCounts.forEach(it -> it.setReportExecution(finalReportExecution));
        reportCountDao.save(reportCounts);

    }

}
