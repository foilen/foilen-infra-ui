/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.foilen.infra.plugin.core.system.junits.JunitsHelper;
import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.context.internal.InternalServicesContext;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.core.service.internal.InternalChangeService;
import com.foilen.infra.resource.example.JunitResource;
import com.foilen.infra.resource.example.JunitResourceEnum;
import com.foilen.infra.ui.db.dao.PluginResourceColumnSearchDao;
import com.foilen.infra.ui.db.dao.PluginResourceDao;
import com.foilen.infra.ui.db.domain.plugin.PluginResourceColumnSearch;
import com.foilen.infra.ui.test.AbstractSpringTests;
import com.foilen.smalltools.test.asserts.AssertTools;

public class ResourceManagementServiceImplTest extends AbstractSpringTests {

    @Autowired
    private CommonServicesContext commonServicesContext;
    @Autowired
    private InternalChangeService internalChangeService;
    @Autowired
    private InternalServicesContext internalServicesContext;
    @Autowired
    private PluginResourceDao pluginResourceDao;
    @Autowired
    private PluginResourceColumnSearchDao pluginResourceColumnSearchDao;
    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private IPResourceService resourceService;

    public ResourceManagementServiceImplTest() {
        super(true);
    }

    @Override
    @Before
    public void createFakeData() {
        super.createFakeData();
        JunitsHelper.createFakeData(commonServicesContext, internalServicesContext);
    }

    @Test
    public void testCreateResource() {

        // Clear all data
        fakeDataService.clearAll();

        // Insert one resource
        JunitResource junitResource = new JunitResource( //
                "myText", //
                JunitResourceEnum.B, //
                new Date(1262322000000L), //
                45, //
                18L, //
                5.67, //
                5.67F, //
                true, //
                "setOne", "setTwo");

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                ChangesContext changes = new ChangesContext(resourceService);
                changes.resourceAdd(junitResource);
                internalChangeService.changesExecute(changes);
            }
        });

        // Asserts
        Assert.assertEquals(1, pluginResourceDao.count());

        List<PluginResourceColumnSearch> columnSearches = pluginResourceColumnSearchDao.findAll(new Sort("columnName"));
        columnSearches.forEach(it -> {
            it.setId(null);
            it.setPluginResource(null);
        });
        AssertTools.assertJsonComparison("PluginResourceServiceImplTest-testCreateResource-expected.json", getClass(), columnSearches);
    }

}
