/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.foilen.infra.api.response.ResponseResourceAppliedChanges;
import com.foilen.infra.plugin.core.system.junits.JunitsHelper;
import com.foilen.infra.plugin.core.system.mongodb.repositories.PluginResourceRepository;
import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.context.internal.InternalServicesContext;
import com.foilen.infra.plugin.v1.core.resource.IPResourceDefinition;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.core.service.internal.InternalChangeService;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.infra.resource.example.JunitResource;
import com.foilen.infra.resource.example.JunitResourceEnum;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.ui.MetaConstants;
import com.foilen.infra.ui.test.AbstractSpringTests;
import com.foilen.infra.ui.test.mock.FakeDataServiceImpl;
import com.foilen.infra.ui.visual.ResourceTypeAndDetails;
import com.foilen.smalltools.test.asserts.AssertTools;
import com.foilen.smalltools.tools.JsonTools;
import com.google.common.collect.ComparisonChain;

public class ResourceManagementServiceImplTest extends AbstractSpringTests {

    @Autowired
    private CommonServicesContext commonServicesContext;
    @Autowired
    private InternalChangeService internalChangeService;
    @Autowired
    private InternalServicesContext internalServicesContext;
    @Autowired
    private PluginResourceRepository pluginResourceRepository;
    @Autowired
    private ResourceManagementService resourceManagementService;
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

    private Map<String, Long> sortAndSet1Long(Map<String, Long> map) {
        Map<String, Long> newMap = new TreeMap<>(map);
        newMap.keySet().stream().forEach(it -> newMap.put(it, 1L));
        return newMap;
    }

    @Test
    public void testApiChangesExecute() {

        // Clear all data
        fakeDataService.clearAll();

        // Empty
        ChangesContext changesContext = new ChangesContext(resourceService);
        changesContext.resourceAdd(new Machine("localhost.example.com", "127.0.0.1"));
        ResponseResourceAppliedChanges responseResourceAppliedChanges = new ResponseResourceAppliedChanges();
        resourceManagementService.changesExecute(changesContext, null, responseResourceAppliedChanges);

        responseResourceAppliedChanges.setExecutionTimeInMsByActionHandler(sortAndSet1Long(responseResourceAppliedChanges.getExecutionTimeInMsByActionHandler()));

        Assert.assertNotNull(responseResourceAppliedChanges.getTxId());
        responseResourceAppliedChanges.setTxId("was set");

        Collections.sort(responseResourceAppliedChanges.getAuditItems().getItems(), (a, b) -> JsonTools.compactPrintWithoutNulls(a).compareTo(JsonTools.compactPrintWithoutNulls(b)));

        responseResourceAppliedChanges.setExecutionTimeInMsByActionHandler(new TreeMap<>(responseResourceAppliedChanges.getExecutionTimeInMsByActionHandler()));
        responseResourceAppliedChanges.setUpdateCountByResourceId(new TreeMap<>(responseResourceAppliedChanges.getUpdateCountByResourceId()));
        responseResourceAppliedChanges.getExecutionTimeInMsByActionHandler().keySet().stream().collect(Collectors.toList()).forEach(key -> {
            int pos = key.indexOf("Lambda$");
            if (pos != -1) {
                pos += 7;
                String newKey = key.substring(0, pos) + "NUM";
                Map<String, Long> executionTimeInMsByActionHandler = responseResourceAppliedChanges.getExecutionTimeInMsByActionHandler();
                executionTimeInMsByActionHandler.put(newKey, executionTimeInMsByActionHandler.get(key));
                executionTimeInMsByActionHandler.remove(key);
            }
        });
        AssertTools.assertJsonComparison("ResourceManagementServiceImplTest-testApiChangesExecute-expected.json", getClass(), responseResourceAppliedChanges);

    }

    @Test
    public void testApiChangesExecute_empty() {

        // Clear all data
        fakeDataService.clearAll();

        // Empty
        ChangesContext changesContext = new ChangesContext(resourceService);
        ResponseResourceAppliedChanges responseResourceAppliedChanges = new ResponseResourceAppliedChanges();
        resourceManagementService.changesExecute(changesContext, null, responseResourceAppliedChanges);

        Assert.assertNotNull(responseResourceAppliedChanges.getTxId());
        responseResourceAppliedChanges.setTxId("was set");
        AssertTools.assertJsonComparison("ResourceManagementServiceImplTest-testApiChangesExecute_empty-expected.json", getClass(), responseResourceAppliedChanges);

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
        Assert.assertEquals(1, pluginResourceRepository.count());
    }

    @Test
    public void testResourceFindAll_admin() {

        List<ResourceTypeAndDetails> resourcesTypeAndDetails = testResourceFindAllForUser(FakeDataServiceImpl.USER_ID_ADMIN);

        AssertTools.assertJsonComparison("ResourceManagementServiceImplTest-testResourceFindAll_admin-expected.json", getClass(), resourcesTypeAndDetails);

    }

    @Test
    public void testResourceFindAll_alphaUser() {

        List<ResourceTypeAndDetails> resourcesTypeAndDetails = testResourceFindAllForUser(FakeDataServiceImpl.USER_ID_ALPHA);

        AssertTools.assertJsonComparison("ResourceManagementServiceImplTest-testResourceFindAll_alphaUser-expected.json", getClass(), resourcesTypeAndDetails);

    }

    @Test
    public void testResourceFindAll_nopermUser() {

        List<ResourceTypeAndDetails> resourcesTypeAndDetails = testResourceFindAllForUser(FakeDataServiceImpl.USER_ID_NOPERM);

        AssertTools.assertJsonComparison(Collections.emptyList(), resourcesTypeAndDetails);

    }

    private List<ResourceTypeAndDetails> testResourceFindAllForUser(String userId) {
        // Change all junits to owned by alpha
        ChangesContext changes = new ChangesContext(resourceService);
        internalServicesContext.getInternalIPResourceService().resourceFindAll().stream() //
                .filter(it -> it instanceof JunitResource) //
                .forEach(it -> {
                    it.getMeta().put(MetaConstants.META_OWNER, FakeDataServiceImpl.OWNER_ALPHA);
                    changes.resourceUpdate(it);
                });
        internalChangeService.changesExecute(changes);

        // Execute
        List<IPResourceDefinition> resourceDefinitions = resourceService.getResourceDefinitions();

        Stream<ResourceTypeAndDetails> resourcesTypeAndDetailsStream = Stream.of();

        for (IPResourceDefinition resourceDefinition : resourceDefinitions) {
            String resourceType = resourceDefinition.getResourceType();
            Stream<IPResource> resources = resourceManagementService.resourceFindAll(userId, resourceService.createResourceQuery(resourceType)).stream();

            resourcesTypeAndDetailsStream = Stream.concat(resourcesTypeAndDetailsStream, resources.map(it -> new ResourceTypeAndDetails(resourceType, it)));

        }

        // Sorting
        resourcesTypeAndDetailsStream = resourcesTypeAndDetailsStream.sorted((a, b) -> ComparisonChain.start() //
                .compare(a.getType(), b.getType()) //
                .compare(a.getResource().getResourceName(), b.getResource().getResourceName()) //
                .compare(a.getResource().getResourceDescription(), b.getResource().getResourceDescription()) //
                .result() //
        );

        List<ResourceTypeAndDetails> resourcesTypeAndDetails = resourcesTypeAndDetailsStream.collect(Collectors.toList());
        return resourcesTypeAndDetails;
    }

}
