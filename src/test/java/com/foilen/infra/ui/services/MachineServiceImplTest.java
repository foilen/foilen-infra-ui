/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
 package com.foilen.infra.ui.services;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.foilen.infra.api.model.MachineSetup;
import com.foilen.infra.ui.test.AbstractSpringTests;
import com.foilen.smalltools.test.asserts.AssertTools;
import com.foilen.smalltools.tools.JsonTools;

public class MachineServiceImplTest extends AbstractSpringTests {

    @Autowired
    private MachineService machineService;

    public MachineServiceImplTest() {
        super(true);
    }

    @Test
    public void testGetMachineSetup_f001() {

        MachineSetup machineSetup = machineService.getMachineSetup("f001.node.example.com");
        Assert.assertNotNull(machineSetup.getUiApiUserId());
        Assert.assertNotNull(machineSetup.getUiApiUserKey());
        machineSetup.setUiApiUserId("_SET_");
        machineSetup.setUiApiUserKey("_SET_");

        MachineSetup expectedMachineSetup = JsonTools.readFromResource("MachineServiceImplTest-testGetMachineSetup-f001.json", MachineSetup.class, getClass());

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
