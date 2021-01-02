/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.foilen.infra.ui.repositories.ReportExecutionRepository;
import com.foilen.infra.ui.repositories.documents.ReportExecution;
import com.foilen.infra.ui.repositories.documents.models.ReportCount;
import com.foilen.infra.ui.repositories.documents.models.ReportTime;
import com.foilen.infra.ui.test.AbstractSpringTests;

public class ReportServiceImplTest extends AbstractSpringTests {

    @Autowired
    private ReportService reportService;
    @Autowired
    private ReportExecutionRepository reportExecutionRepository;

    public ReportServiceImplTest() {
        super(false);
    }

    @Test
    public void testAddReport() {

        reportService.addReport("tx1", true, //
                Arrays.asList(new ReportTime("class1", 1000), new ReportTime("class2", 1500), new ReportTime("class3", 500)), //
                Arrays.asList(new ReportCount("resource1", 1), new ReportCount("resource2", 2)) //
        );

        Assert.assertEquals(1, reportExecutionRepository.count());
        ReportExecution reportExecution = reportExecutionRepository.findAll().stream().findAny().get();
        Assert.assertEquals(3, reportExecution.getReportTimes().size());
        Assert.assertEquals(2, reportExecution.getReportCounts().size());

    }

}
