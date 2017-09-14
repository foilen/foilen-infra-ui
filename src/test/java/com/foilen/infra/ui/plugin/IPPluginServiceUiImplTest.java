/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
 package com.foilen.infra.ui.plugin;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.foilen.infra.plugin.core.system.junits.JunitsHelper;
import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.context.internal.InternalServicesContext;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.core.service.internal.InternalChangeService;
import com.foilen.infra.plugin.v1.model.junit.JunitResource;
import com.foilen.infra.plugin.v1.model.junit.JunitResourceEnum;
import com.foilen.infra.ui.db.dao.PluginResourceColumnSearchDao;
import com.foilen.infra.ui.db.dao.PluginResourceDao;
import com.foilen.infra.ui.db.domain.plugin.PluginResource;
import com.foilen.infra.ui.db.domain.plugin.PluginResourceColumnSearch;
import com.foilen.infra.ui.test.AbstractSpringTests;
import com.foilen.smalltools.tools.DateTools;

public class IPPluginServiceUiImplTest extends AbstractSpringTests {

    @Autowired
    private CommonServicesContext commonServicesContext;
    @Autowired
    private InternalChangeService internalChangeService;
    @Autowired
    private InternalServicesContext internalServicesContext;
    @Autowired
    private IPResourceService resourceService;
    @Autowired
    private PluginResourceDao pluginResourceDao;
    @Autowired
    private PluginResourceColumnSearchDao pluginResourceColumnSearchDao;
    @Autowired
    private IPPluginServiceUiImpl ipPluginServiceImpl;

    public IPPluginServiceUiImplTest() {
        super(false);
    }

    @Override
    @Before
    public void createFakeData() {
        super.createFakeData();

        JunitsHelper.addResourcesDefinition(internalServicesContext);

        Assert.assertEquals(0, pluginResourceDao.count());
        Assert.assertEquals(0, pluginResourceColumnSearchDao.count());

        // Create some items
        ChangesContext changes = new ChangesContext(resourceService);
        changes.resourceAdd(new JunitResource("t1_aaa", JunitResourceEnum.C, DateTools.parseFull("2000-01-01 00:00:00"), 1, 12L, 123.0, 1.234f, true, "one", "two"));
        changes.resourceAdd(new JunitResource("t1_bbb", JunitResourceEnum.B, 1));
        internalChangeService.changesExecute(changes);

        Assert.assertEquals(2, pluginResourceDao.count());
        Assert.assertEquals(18, pluginResourceColumnSearchDao.count());
    }

    private void process() {
        ipPluginServiceImpl.updateResourcesColumnSearch(commonServicesContext.getResourceService().getResourceDefinitions());

        Assert.assertEquals(2, pluginResourceDao.count());
        Assert.assertEquals(18, pluginResourceColumnSearchDao.count());
    }

    @Test
    public void testUpdateResourcesColumnSearchListOfIPResourceDefinition_ExtraColumn() {

        JunitsHelper.createFakeDataWithSets(commonServicesContext, internalServicesContext);

        // Collect all the column searches ids
        List<Long> columnSearchIds = pluginResourceColumnSearchDao.findAll().stream() //
                .map(PluginResourceColumnSearch::getId) //
                .sorted() //
                .collect(Collectors.toList());
        Assert.assertEquals(100, columnSearchIds.size());

        // Create extra column data
        for (PluginResource pluginResource : pluginResourceDao.findAll()) {
            PluginResourceColumnSearch pluginResourceColumnSearch = new PluginResourceColumnSearch(pluginResource, "bad");
            pluginResourceColumnSearch.setText("blah");
            pluginResourceColumnSearchDao.saveAndFlush(pluginResourceColumnSearch);
        }
        Assert.assertEquals(107, pluginResourceColumnSearchDao.count());

        // Process
        ipPluginServiceImpl.updateResourcesColumnSearch(commonServicesContext.getResourceService().getResourceDefinitions());

        Assert.assertEquals(7, pluginResourceDao.count());
        Assert.assertEquals(100, pluginResourceColumnSearchDao.count());

        // Check the column searches ids are still the same (for perf optimization)
        List<Long> finalColumnSearchIds = pluginResourceColumnSearchDao.findAll().stream() //
                .map(PluginResourceColumnSearch::getId) //
                .sorted() //
                .collect(Collectors.toList());
        Assert.assertEquals(100, finalColumnSearchIds.size());
        Assert.assertEquals(columnSearchIds, finalColumnSearchIds);
    }

    @Test
    public void testUpdateResourcesColumnSearchListOfIPResourceDefinition_MissingColumn() {
        // Delete 2 columns
        List<PluginResourceColumnSearch> toDelete = pluginResourceColumnSearchDao.findAll().stream() //
                .filter(it -> {
                    return it.getColumnName().equals("setTexts") || it.getColumnName().equals("longNumber");
                }) //
                .collect(Collectors.toList());
        pluginResourceColumnSearchDao.delete(toDelete);

        // Process
        process();

    }

    @Test
    public void testUpdateResourcesColumnSearchListOfIPResourceDefinition_MissingRows() {

        // Delete column searches for one resource
        Long resourceIdToDelete = pluginResourceDao.findAll().get(0).getId();
        List<PluginResourceColumnSearch> toDelete = pluginResourceColumnSearchDao.findAll().stream() //
                .filter(it -> {
                    return resourceIdToDelete.equals(it.getPluginResource().getId());
                }) //
                .collect(Collectors.toList());
        pluginResourceColumnSearchDao.delete(toDelete);
        Assert.assertEquals(8, pluginResourceColumnSearchDao.count());

        // Process
        process();

    }

}
