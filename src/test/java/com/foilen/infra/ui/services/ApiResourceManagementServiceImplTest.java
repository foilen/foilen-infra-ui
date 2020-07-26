/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.foilen.infra.api.model.resource.LinkDetails;
import com.foilen.infra.api.model.resource.ResourceBucket;
import com.foilen.infra.api.model.resource.ResourceDetails;
import com.foilen.infra.api.request.RequestChanges;
import com.foilen.infra.api.request.RequestResourceSearch;
import com.foilen.infra.api.response.ResponseResourceAppliedChanges;
import com.foilen.infra.api.response.ResponseResourceBucket;
import com.foilen.infra.api.response.ResponseResourceBuckets;
import com.foilen.infra.plugin.core.system.junits.JunitsHelper;
import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.context.internal.InternalServicesContext;
import com.foilen.infra.plugin.v1.core.resource.IPResourceDefinition;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.core.service.internal.InternalChangeService;
import com.foilen.infra.plugin.v1.core.service.internal.InternalIPResourceService;
import com.foilen.infra.plugin.v1.model.resource.AbstractIPResource;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.example.JunitResource;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.urlredirection.UrlRedirection;
import com.foilen.infra.ui.MetaConstants;
import com.foilen.infra.ui.services.exception.UserPermissionException;
import com.foilen.infra.ui.test.AbstractSpringTests;
import com.foilen.infra.ui.test.mock.FakeDataServiceImpl;
import com.foilen.smalltools.test.asserts.AssertTools;
import com.foilen.smalltools.tools.JsonTools;
import com.foilen.smalltools.tools.ResourceTools;
import com.google.common.base.Joiner;
import com.google.common.collect.ComparisonChain;

public class ApiResourceManagementServiceImplTest extends AbstractSpringTests {

    private static final Joiner lineReturn = Joiner.on('\n');

    @Autowired
    private ApiResourceManagementService apiResourceManagementService;
    @Autowired
    private CommonServicesContext commonServicesContext;
    @Autowired
    private InternalChangeService internalChangeService;
    @Autowired
    private InternalIPResourceService internalIPResourceService;
    @Autowired
    private InternalServicesContext internalServicesContext;
    @Autowired
    private IPResourceService resourceService;

    public ApiResourceManagementServiceImplTest() {
        super(true);
    }

    private void assertResourcesOwnership(List<String> initialResourceOwners) {
        List<String> actualOwners = getResourcesOwnership();
        actualOwners.removeAll(initialResourceOwners);

        String expected = ResourceTools.getResourceAsString("ApiResourceManagementServiceImplTest-assertResourcesOwnership.txt", getClass());
        Assert.assertEquals(expected, lineReturn.join(actualOwners));
    }

    @SuppressWarnings("unchecked")
    private void clearInternalId(ResourceDetails resourceDetails) {
        if (resourceDetails.getResource() instanceof Map) {
            Map<String, Object> resource = (Map<String, Object>) resourceDetails.getResource();
            if (resource.containsKey("internalId")) {
                resource.put("internalId", "--SET--");
            }
        } else {
            AbstractIPResource resource = (AbstractIPResource) resourceDetails.getResource();
            if (resource.getInternalId() != null) {
                resource.setInternalId("--SET--");
            }
        }
    }

    @Override
    @Before
    public void createFakeData() {
        super.createFakeData();
        JunitsHelper.createFakeData(commonServicesContext, internalServicesContext);
    }

    private List<String> getResourcesOwnership() {
        List<String> actualOwners = internalIPResourceService.resourceFindAll().stream() //
                .map(r -> r.getClass() + " ; " + r.getResourceName() + " ; " + r.getMeta().get(MetaConstants.META_OWNER)) //
                .sorted() //
                .collect(Collectors.toList());
        return actualOwners;
    }

    @Test
    public void testApplyChanges_api_admin() {
        setApiAuth(FakeDataServiceImpl.API_USER_ID_ADMIN);
        testApplyChangesExecute("f001.node.example.com", FakeDataServiceImpl.API_USER_ID_ADMIN);
    }

    @Test
    public void testApplyChanges_api_noperm() {

        setApiAuth("noperm");

        try {
            testApplyChangesExecute("f001.node.example.com", "noperm");
            Assert.fail("Expecting UserPermissionException");
        } catch (UserPermissionException e) {
            // Expected
        } catch (Exception e) {
            Assert.fail("Expecting UserPermissionException");
        }
    }

    @Test
    public void testApplyChanges_api_without_machine_perms() {

        setApiAuth(FakeDataServiceImpl.API_USER_ID_USER_ALPHA_NO_MACHINE);

        try {
            testApplyChangesExecute("f002.node.example.com", FakeDataServiceImpl.API_USER_ID_USER_ALPHA_NO_MACHINE);
            Assert.fail("Expecting UserPermissionException");
        } catch (UserPermissionException e) {
            // Expected
        } catch (Exception e) {
            Assert.fail("Expecting UserPermissionException");
        }
    }

    @Test
    public void testApplyChanges_api_withperms() {

        setApiAuth(FakeDataServiceImpl.API_USER_ID_USER_ALPHA);

        List<String> initialOwners = getResourcesOwnership();

        testApplyChangesExecute("f001.node.example.com", FakeDataServiceImpl.API_USER_ID_USER_ALPHA);

        assertResourcesOwnership(initialOwners);
    }

    @Test
    public void testApplyChanges_api_wrong_owner() {

        setApiAuth(FakeDataServiceImpl.API_USER_ID_USER_BETA);

        try {
            testApplyChangesExecute("f001.node.example.com", FakeDataServiceImpl.API_USER_ID_USER_BETA);
            Assert.fail("Expecting UserPermissionException");
        } catch (UserPermissionException e) {
            // Expected
        } catch (Exception e) {
            Assert.fail("Expecting UserPermissionException");
        }
    }

    @Test
    public void testApplyChanges_human_admin() {
        setFoilenAuth(FakeDataServiceImpl.USER_ID_ADMIN);
        testApplyChangesExecute("f001.node.example.com", FakeDataServiceImpl.USER_ID_ADMIN);
    }

    @Test
    public void testApplyChanges_human_noperm() {

        setFoilenAuth("noperm");

        try {
            testApplyChangesExecute("f001.node.example.com", "noperm");
            Assert.fail("Expecting UserPermissionException");
        } catch (UserPermissionException e) {
            // Expected
        } catch (Exception e) {
            Assert.fail("Expecting UserPermissionException");
        }
    }

    @Test
    public void testApplyChanges_human_without_machine_perms() {

        setFoilenAuth(FakeDataServiceImpl.USER_ID_ALPHA);

        try {
            testApplyChangesExecute("f002.node.example.com", FakeDataServiceImpl.USER_ID_ALPHA);
            Assert.fail("Expecting UserPermissionException");
        } catch (UserPermissionException e) {
            // Expected
        } catch (Exception e) {
            Assert.fail("Expecting UserPermissionException");
        }
    }

    @Test
    public void testApplyChanges_human_withperms() {

        setFoilenAuth(FakeDataServiceImpl.USER_ID_ALPHA);

        List<String> initialOwners = getResourcesOwnership();

        testApplyChangesExecute("f001.node.example.com", FakeDataServiceImpl.USER_ID_ALPHA);

        assertResourcesOwnership(initialOwners);
    }

    @Test
    public void testApplyChanges_human_wrong_owner() {

        setFoilenAuth(FakeDataServiceImpl.USER_ID_BETA);

        try {
            testApplyChangesExecute("f001.node.example.com", FakeDataServiceImpl.USER_ID_BETA);
            Assert.fail("Expecting UserPermissionException");
        } catch (UserPermissionException e) {
            // Expected
        } catch (Exception e) {
            Assert.fail("Expecting UserPermissionException");
        }
    }

    private void testApplyChangesExecute(String machineName, String userId) {
        RequestChanges changes = new RequestChanges();

        changes.setDefaultOwner(FakeDataServiceImpl.OWNER_ALPHA);

        UrlRedirection urlRedirection = new UrlRedirection("myredir.foilen-lab.com");
        urlRedirection.getMeta().put(MetaConstants.META_OWNER, FakeDataServiceImpl.OWNER_ALPHA);
        urlRedirection.setHttpRedirectToUrl("https://myredir.foilen-lab.com");
        urlRedirection.setHttpIsPermanent(true);
        changes.getResourcesToAdd().add(new ResourceDetails(UrlRedirection.RESOURCE_TYPE, urlRedirection));

        changes.getLinksToAdd().add(new LinkDetails( //
                new ResourceDetails(UrlRedirection.RESOURCE_TYPE, urlRedirection), //
                LinkTypeConstants.INSTALLED_ON, //
                new ResourceDetails(Machine.RESOURCE_TYPE, new Machine(machineName))));

        ResponseResourceAppliedChanges responseResourceAppliedChanges = apiResourceManagementService.applyChanges(userId, changes);
        if (!responseResourceAppliedChanges.isSuccess()) {
            if (responseResourceAppliedChanges.getGlobalErrors().stream().anyMatch(it -> it.contains("permission"))) {
                throw new UserPermissionException(JsonTools.prettyPrintWithoutNulls(responseResourceAppliedChanges));
            }
            throw new RuntimeException(JsonTools.prettyPrintWithoutNulls(responseResourceAppliedChanges));
        }
    }

    @Test
    public void testResourceFindAll_admin() {

        List<ResourceDetails> resourcesTypeAndDetails = testResourceFindAllForUser(FakeDataServiceImpl.USER_ID_ADMIN);

        AssertTools.assertJsonComparison("ApiResourceManagementServiceImplTest-testResourceFindAll_admin-expected.json", getClass(), resourcesTypeAndDetails);

    }

    @Test
    public void testResourceFindAll_alphaUser() {

        List<ResourceDetails> resourcesTypeAndDetails = testResourceFindAllForUser(FakeDataServiceImpl.USER_ID_ALPHA);

        AssertTools.assertJsonComparison("ApiResourceManagementServiceImplTest-testResourceFindAll_alphaUser-expected.json", getClass(), resourcesTypeAndDetails);

    }

    @Test
    public void testResourceFindAll_nopermUser() {

        List<ResourceDetails> resourcesTypeAndDetails = testResourceFindAllForUser(FakeDataServiceImpl.USER_ID_NOPERM);

        AssertTools.assertJsonComparison(Collections.emptyList(), resourcesTypeAndDetails);

    }

    @SuppressWarnings("unchecked")
    private List<ResourceDetails> testResourceFindAllForUser(String userId) {
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

        Stream<ResourceDetails> resourceDetailsStream = Stream.of();

        for (IPResourceDefinition resourceDefinition : resourceDefinitions) {
            String resourceType = resourceDefinition.getResourceType();
            Stream<ResourceBucket> resources = apiResourceManagementService.resourceFindAll(userId, new RequestResourceSearch().setResourceType(resourceType)).getItems().stream();

            resourceDetailsStream = Stream.concat(resourceDetailsStream, resources.map(it -> it.getResourceDetails()));

        }

        // Sorting
        resourceDetailsStream = resourceDetailsStream.sorted((a, b) -> {
            Map<String, Object> ra = JsonTools.clone(a.getResource(), Map.class);
            Map<String, Object> rb = JsonTools.clone(b.getResource(), Map.class);
            return ComparisonChain.start() //
                    .compare(a.getResourceType(), b.getResourceType()) //
                    .compare(ra.get("resourceName").toString(), rb.get("resourceName").toString()) //
                    .compare(ra.get("resourceDescription").toString(), rb.get("resourceDescription").toString()) //
                    .result();
        } //
        );

        // Clear the changing id
        return resourceDetailsStream.peek(r -> {
            Map<String, Object> resource = (Map<String, Object>) r.getResource();
            if (resource.containsKey("internalId")) {
                resource.put("internalId", "--SET--");
            }
        }).collect(Collectors.toList());
    }

    @Test
    public void testResourceFindAllWithDetails_admin() {

        List<ResourceDetails> resourcesTypeAndDetails = testResourceFindAllWithDetailsForUser(FakeDataServiceImpl.USER_ID_ADMIN);

        AssertTools.assertJsonComparison("ApiResourceManagementServiceImplTest-testResourceFindAllWithDetails_admin-expected.json", getClass(), resourcesTypeAndDetails);

    }

    @Test
    public void testResourceFindAllWithDetails_alphaUser() {

        List<ResourceDetails> resourcesTypeAndDetails = testResourceFindAllWithDetailsForUser(FakeDataServiceImpl.USER_ID_ALPHA);

        AssertTools.assertJsonComparison("ApiResourceManagementServiceImplTest-testResourceFindAllWithDetails_alphaUser-expected.json", getClass(), resourcesTypeAndDetails);

    }

    @Test
    public void testResourceFindAllWithDetails_nopermUser() {

        List<ResourceDetails> resourcesTypeAndDetails = testResourceFindAllWithDetailsForUser(FakeDataServiceImpl.USER_ID_NOPERM);

        AssertTools.assertJsonComparison(Collections.emptyList(), resourcesTypeAndDetails);

    }

    @SuppressWarnings("unchecked")
    private List<ResourceDetails> testResourceFindAllWithDetailsForUser(String userId) {
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

        Stream<ResourceDetails> resourceDetailsStream = Stream.of();

        for (IPResourceDefinition resourceDefinition : resourceDefinitions) {
            String resourceType = resourceDefinition.getResourceType();
            Stream<ResourceBucket> resources = apiResourceManagementService.resourceFindAllWithDetails(userId, new RequestResourceSearch().setResourceType(resourceType)).getItems().stream();

            resourceDetailsStream = Stream.concat(resourceDetailsStream, resources.map(it -> it.getResourceDetails()));

        }

        // Sorting
        resourceDetailsStream = resourceDetailsStream.sorted((a, b) -> {
            Map<String, Object> ra = JsonTools.clone(a.getResource(), Map.class);
            Map<String, Object> rb = JsonTools.clone(b.getResource(), Map.class);
            return ComparisonChain.start() //
                    .compare(a.getResourceType(), b.getResourceType()) //
                    .compare(ra.get("resourceName").toString(), rb.get("resourceName").toString()) //
                    .compare(ra.get("resourceDescription").toString(), rb.get("resourceDescription").toString()) //
                    .result();
        } //
        );

        // Clear the changing id
        return resourceDetailsStream.peek(r -> {

            if (r.getResource() instanceof Map) {
                Map<String, Object> resource = (Map<String, Object>) r.getResource();
                if (resource.containsKey("internalId")) {
                    resource.put("internalId", "--SET--");
                }
            } else {
                AbstractIPResource resource = (AbstractIPResource) r.getResource();
                if (resource.getInternalId() != null) {
                    resource.setInternalId("--SET--");
                }
            }
        }).collect(Collectors.toList());
    }

    @Test
    public void testResourceFindAllWithoutOwner() {
        ResponseResourceBuckets result = apiResourceManagementService.resourceFindAllWithoutOwner(FakeDataServiceImpl.USER_ID_ADMIN);
        result.getItems().forEach(r -> {
            AbstractIPResource resource = (AbstractIPResource) r.getResourceDetails().getResource();
            if (resource.getInternalId() != null) {
                resource.setInternalId("--SET--");
            }
        });
        AssertTools.assertJsonComparisonWithoutNulls("ApiResourceManagementServiceImplTest-testResourceFindAllWithoutOwner.json", getClass(), result);
    }

    @Test
    public void testResourceFindById_application_admin() {

        // Find the application
        Application application = resourceService.resourceFindByPk(new Application("f1")).get();

        // Execute
        ResponseResourceBucket responseResourceBucket = apiResourceManagementService.resourceFindById(FakeDataServiceImpl.USER_ID_ADMIN, application.getInternalId());

        // Clear the changing ids
        clearInternalId(responseResourceBucket.getItem().getResourceDetails());
        responseResourceBucket.getItem().getLinksFrom().forEach(i -> clearInternalId(i.getOtherResource()));
        responseResourceBucket.getItem().getLinksTo().forEach(i -> clearInternalId(i.getOtherResource()));

        AssertTools.assertJsonComparison("ApiResourceManagementServiceImplTest-testResourceFindById_application_admin-expected.json", getClass(), responseResourceBucket);

    }

    @Test
    public void testResourceFindById_machine_admin() {

        // Find the machine
        Machine machine = resourceService.resourceFindByPk(new Machine("f001.node.example.com")).get();

        // Execute
        ResponseResourceBucket responseResourceBucket = apiResourceManagementService.resourceFindById(FakeDataServiceImpl.USER_ID_ADMIN, machine.getInternalId());

        // Clear the changing ids
        clearInternalId(responseResourceBucket.getItem().getResourceDetails());
        responseResourceBucket.getItem().getLinksFrom().forEach(i -> clearInternalId(i.getOtherResource()));
        responseResourceBucket.getItem().getLinksTo().forEach(i -> clearInternalId(i.getOtherResource()));

        AssertTools.assertJsonComparison("ApiResourceManagementServiceImplTest-testResourceFindById_machine_admin-expected.json", getClass(), responseResourceBucket);

    }

}
