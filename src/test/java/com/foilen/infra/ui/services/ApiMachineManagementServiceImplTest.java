/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.foilen.infra.api.model.MachineSetup;
import com.foilen.infra.api.response.ResponseMachineSetup;
import com.foilen.infra.ui.localonly.FakeDataServiceImpl;
import com.foilen.infra.ui.test.AbstractSpringTests;
import com.foilen.smalltools.test.asserts.AssertTools;

public class ApiMachineManagementServiceImplTest extends AbstractSpringTests {

    private static final String MACHINE_NAME = "f001.node.example.com";
    @Autowired
    private ApiMachineManagementService apiMachineManagementService;

    public ApiMachineManagementServiceImplTest() {
        super(true);
    }

    private void testGetMachineSetup_FAIL(String userId, List<String> expectedErrors, List<String> expectedWarnings) {
        testGetMachineSetup_FAIL(MACHINE_NAME, userId, expectedErrors, expectedWarnings);
    }

    private void testGetMachineSetup_FAIL(String machineName, String userId, List<String> expectedErrors, List<String> expectedWarnings) {
        ResponseMachineSetup result = apiMachineManagementService.getMachineSetup(userId, machineName);

        ResponseMachineSetup expected = new ResponseMachineSetup();
        expected.setErrors(expectedErrors);
        expected.setWarnings(expectedWarnings);

        AssertTools.assertJsonComparisonWithoutNulls(expected, result);
    }

    @Test
    public void testGetMachineSetup_FAIL_ApiMachineUser_another_one() {
        testGetMachineSetup_FAIL(FakeDataServiceImpl.API_USER_MACHINE_ID_F002, Arrays.asList("You are not allowed"), Arrays.asList());
    }

    @Test
    public void testGetMachineSetup_FAIL_ApiUser_Not_Admin() {
        testGetMachineSetup_FAIL(FakeDataServiceImpl.API_USER_ID_USER, Arrays.asList("You are not allowed"), Arrays.asList());
    }

    @Test
    public void testGetMachineSetup_FAIL_MachineNotExists() {
        testGetMachineSetup_FAIL("not.node.example.com", FakeDataServiceImpl.USER_ID_ADMIN, Arrays.asList("The machine does not exist"), Arrays.asList());
    }

    @Test
    public void testGetMachineSetup_FAIL_Noone() {
        testGetMachineSetup_FAIL(null, Arrays.asList("You are not allowed"), Arrays.asList());
    }

    @Test
    public void testGetMachineSetup_FAIL_User_Not_Admin() {
        testGetMachineSetup_FAIL(FakeDataServiceImpl.USER_ID_USER, Arrays.asList("You are not allowed"), Arrays.asList());
    }

    private void testGetMachineSetup_OK(String userId) {
        ResponseMachineSetup result = apiMachineManagementService.getMachineSetup(userId, MACHINE_NAME);

        if (result.getItem() != null) {
            result.setItem(new MachineSetup());
        }

        ResponseMachineSetup expected = new ResponseMachineSetup();
        expected.setItem(new MachineSetup());

        AssertTools.assertJsonComparisonWithoutNulls(expected, result);
    }

    @Test
    public void testGetMachineSetup_OK_ApiMachineUser_right_one() {
        testGetMachineSetup_OK(FakeDataServiceImpl.API_USER_MACHINE_ID_F001);
    }

    @Test
    public void testGetMachineSetup_OK_ApiUser_Admin() {
        testGetMachineSetup_OK(FakeDataServiceImpl.API_USER_ID_ADMIN);
    }

    @Test
    public void testGetMachineSetup_OK_User_Admin() {
        testGetMachineSetup_OK(FakeDataServiceImpl.USER_ID_ADMIN);
    }

}