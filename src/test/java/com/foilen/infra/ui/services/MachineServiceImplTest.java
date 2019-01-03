/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.foilen.infra.api.model.MachineSetup;
import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.core.service.internal.InternalChangeService;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.unixuser.SystemUnixUser;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.foilen.infra.ui.test.AbstractSpringTests;
import com.foilen.smalltools.test.asserts.AssertTools;
import com.foilen.smalltools.tools.JsonTools;

public class MachineServiceImplTest extends AbstractSpringTests {

    @Autowired
    private InternalChangeService internalChangeService;
    @Autowired
    private IPResourceService ipResourceService;
    @Autowired
    private MachineService machineService;

    public MachineServiceImplTest() {
        super(true);
    }

    @Test
    public void testGetMachineSetup_f001() {

        // Add a system user that must not be returned
        ChangesContext changes = new ChangesContext(ipResourceService);
        changes.linkAdd(new SystemUnixUser(0L, "root"), LinkTypeConstants.INSTALLED_ON, new Machine("f001.node.example.com"));
        internalChangeService.changesExecute(changes);

        MachineSetup machineSetup = machineService.getMachineSetup("f001.node.example.com");
        Assert.assertNotNull(machineSetup.getUiApiUserId());
        Assert.assertNotNull(machineSetup.getUiApiUserKey());
        machineSetup.setUiApiUserId("_SET_");
        machineSetup.setUiApiUserKey("_SET_");

        MachineSetup expectedMachineSetup = JsonTools.readFromResource("MachineServiceImplTest-testGetMachineSetup-f001.json", MachineSetup.class, getClass());

        AssertTools.assertIgnoreLineFeed(JsonTools.prettyPrint(expectedMachineSetup), JsonTools.prettyPrint(machineSetup));

    }

    @Test
    public void testGetMachineSetup_f001_withInfraDockerManagerUser() {

        ChangesContext changes = new ChangesContext(ipResourceService);
        changes.resourceAdd(new UnixUser(123456L, "infra_docker_manager", "/home/infra_docker_manager", null, null));
        internalChangeService.changesExecute(changes);

        MachineSetup machineSetup = machineService.getMachineSetup("f001.node.example.com");
        Assert.assertNotNull(machineSetup.getUiApiUserId());
        Assert.assertNotNull(machineSetup.getUiApiUserKey());
        machineSetup.setUiApiUserId("_SET_");
        machineSetup.setUiApiUserKey("_SET_");

        MachineSetup expectedMachineSetup = JsonTools.readFromResource("MachineServiceImplTest-testGetMachineSetup_f001_withInfraDockerManagerUser.json", MachineSetup.class, getClass());

        AssertTools.assertIgnoreLineFeed(JsonTools.prettyPrint(expectedMachineSetup), JsonTools.prettyPrint(machineSetup));

    }

    @Test
    public void testGetMachineSetup_f002() {

        MachineSetup machineSetup = machineService.getMachineSetup("f002.node.example.com");
        Assert.assertNotNull(machineSetup.getUiApiUserId());
        Assert.assertNotNull(machineSetup.getUiApiUserKey());
        machineSetup.setUiApiUserId("_SET_");
        machineSetup.setUiApiUserKey("_SET_");

        MachineSetup expectedMachineSetup = JsonTools.readFromResource("MachineServiceImplTest-testGetMachineSetup-f002.json", MachineSetup.class, getClass());

        AssertTools.assertIgnoreLineFeed(JsonTools.prettyPrint(expectedMachineSetup), JsonTools.prettyPrint(machineSetup));

    }

    @Test
    public void testGetMachineSetup_none() {

        MachineSetup machineSetup = machineService.getMachineSetup("none.node.example.com");
        Assert.assertNull(machineSetup);

    }

}
