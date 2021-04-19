/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services.hook;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.core.service.internal.InternalChangeService;
import com.foilen.infra.plugin.v1.core.service.internal.InternalIPResourceService;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.urlredirection.UrlRedirection;
import com.foilen.infra.ui.MetaConstants;
import com.foilen.infra.ui.localonly.FakeDataServiceImpl;
import com.foilen.infra.ui.services.exception.UserPermissionException;
import com.foilen.infra.ui.test.AbstractSpringTests;
import com.foilen.smalltools.tools.ResourceTools;
import com.google.common.base.Joiner;

public class UserPermissionChangeExecutionHookTest extends AbstractSpringTests {

    private static final Joiner lineReturn = Joiner.on('\n');

    @Autowired
    private InternalChangeService internalChangeService;
    @Autowired
    private InternalIPResourceService internalIPResourceService;
    @Autowired
    private IPResourceService resourceService;

    public UserPermissionChangeExecutionHookTest() {
        super(true);
    }

    private void assertResourcesOwnership(List<String> initialResourceOwners) {
        List<String> actualOwners = getResourcesOwnership();
        actualOwners.removeAll(initialResourceOwners);

        String expected = ResourceTools.getResourceAsString("UserPermissionChangeExecutionHookTest-assertResourcesOwnership.txt", getClass());
        Assert.assertEquals(expected, lineReturn.join(actualOwners));
    }

    private void execute(String machineName) {
        ChangesContext changes = new ChangesContext(resourceService);

        UrlRedirection urlRedirection = new UrlRedirection("myredir.foilen-lab.com");
        urlRedirection.getMeta().put(MetaConstants.META_OWNER, FakeDataServiceImpl.OWNER_ALPHA);
        urlRedirection.setHttpRedirectToUrl("https://myredir.foilen-lab.com");
        urlRedirection.setHttpIsPermanent(true);
        changes.resourceAdd(urlRedirection);

        changes.linkAdd(urlRedirection, LinkTypeConstants.INSTALLED_ON, new Machine(machineName));

        internalChangeService.changesExecute(changes, Collections.singletonList(new DefaultOwnerChangeExecutionHook(FakeDataServiceImpl.OWNER_ALPHA)));
    }

    private List<String> getResourcesOwnership() {
        List<String> actualOwners = internalIPResourceService.resourceFindAll().stream() //
                .map(r -> r.getClass() + " ; " + r.getResourceName() + " ; " + r.getMeta().get(MetaConstants.META_OWNER)) //
                .sorted() //
                .collect(Collectors.toList());
        return actualOwners;
    }

    @Test
    public void test_api_admin() {

        setApiAuth(FakeDataServiceImpl.API_USER_ID_ADMIN);

        execute("f001.node.example.com");
    }

    @Test
    public void test_api_noperm() {

        setApiAuth("noperm");

        try {
            execute("f001.node.example.com");
            Assert.fail("Expecting UserPermissionException");
        } catch (UserPermissionException e) {
            // Expected
        } catch (Exception e) {
            Assert.fail("Expecting UserPermissionException");
        }
    }

    @Test
    public void test_api_without_machine_perms() {

        setApiAuth(FakeDataServiceImpl.API_USER_ID_USER_ALPHA_NO_MACHINE);

        try {
            execute("f002.node.example.com");
            Assert.fail("Expecting UserPermissionException");
        } catch (UserPermissionException e) {
            // Expected
        } catch (Exception e) {
            Assert.fail("Expecting UserPermissionException");
        }
    }

    @Test
    public void test_api_withperms() {

        List<String> initialOwners = getResourcesOwnership();

        setApiAuth(FakeDataServiceImpl.API_USER_ID_USER_ALPHA);

        execute("f001.node.example.com");

        assertResourcesOwnership(initialOwners);
    }

    @Test
    public void test_api_wrong_owner() {

        setApiAuth(FakeDataServiceImpl.API_USER_ID_USER_BETA);

        try {
            execute("f001.node.example.com");
            Assert.fail("Expecting UserPermissionException");
        } catch (UserPermissionException e) {
            // Expected
        } catch (Exception e) {
            Assert.fail("Expecting UserPermissionException");
        }
    }

    @Test
    public void test_human_admin() {

        setFoilenAuth(FakeDataServiceImpl.USER_ID_ADMIN);

        execute("f001.node.example.com");
    }

    @Test
    public void test_human_noperm() {

        setFoilenAuth("noperm");

        try {
            execute("f001.node.example.com");
            Assert.fail("Expecting UserPermissionException");
        } catch (UserPermissionException e) {
            // Expected
        } catch (Exception e) {
            Assert.fail("Expecting UserPermissionException");
        }
    }

    @Test
    public void test_human_without_machine_perms() {

        setApiAuth(FakeDataServiceImpl.USER_ID_ALPHA);

        try {
            execute("f002.node.example.com");
            Assert.fail("Expecting UserPermissionException");
        } catch (UserPermissionException e) {
            // Expected
        } catch (Exception e) {
            Assert.fail("Expecting UserPermissionException");
        }
    }

    @Test
    public void test_human_withperms() {

        List<String> initialOwners = getResourcesOwnership();

        setFoilenAuth(FakeDataServiceImpl.USER_ID_ALPHA);

        execute("f001.node.example.com");

        assertResourcesOwnership(initialOwners);
    }

    @Test
    public void test_human_wrong_owner() {

        setFoilenAuth(FakeDataServiceImpl.USER_ID_BETA);

        try {
            execute("f001.node.example.com");
            Assert.fail("Expecting UserPermissionException");
        } catch (UserPermissionException e) {
            // Expected
        } catch (Exception e) {
            Assert.fail("Expecting UserPermissionException");
        }
    }

}
