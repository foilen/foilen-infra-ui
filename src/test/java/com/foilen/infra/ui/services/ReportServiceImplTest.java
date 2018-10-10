/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.foilen.infra.ui.db.dao.ReportCountDao;
import com.foilen.infra.ui.db.dao.ReportExecutionDao;
import com.foilen.infra.ui.db.dao.ReportTimeDao;
import com.foilen.infra.ui.db.domain.reporting.ReportCount;
import com.foilen.infra.ui.db.domain.reporting.ReportTime;
import com.foilen.infra.ui.test.AbstractSpringTests;

public class ReportServiceImplTest extends AbstractSpringTests {

    @Autowired
    private ReportService reportService;
    @Autowired
    private ReportCountDao reportCountDao;
    @Autowired
    private ReportExecutionDao reportExecutionDao;
    @Autowired
    private ReportTimeDao reportTimeDao;

    public ReportServiceImplTest() {
        super(false);
    }

    @Test
    public void testAddReport() {

        reportService.addReport("tx1", true, //
                Arrays.asList(new ReportTime("class1", 1000), new ReportTime("class2", 1500), new ReportTime("class3", 500)), //
                Arrays.asList(new ReportCount("resource1", 1), new ReportCount("resource2", 2)) //
        );

        Assert.assertEquals(1, reportExecutionDao.count());
        Assert.assertEquals(3, reportTimeDao.count());
        Assert.assertEquals(2, reportCountDao.count());

    }

}
