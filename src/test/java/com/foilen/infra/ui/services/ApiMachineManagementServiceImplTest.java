/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.foilen.infra.api.model.machine.MachineSetup;
import com.foilen.infra.api.model.machine.SystemStats;
import com.foilen.infra.api.response.ResponseMachineSetup;
import com.foilen.infra.ui.test.AbstractSpringTests;
import com.foilen.infra.ui.test.mock.FakeDataServiceImpl;
import com.foilen.smalltools.restapi.model.ApiError;
import com.foilen.smalltools.restapi.model.FormResult;
import com.foilen.smalltools.test.asserts.AssertTools;

public class ApiMachineManagementServiceImplTest extends AbstractSpringTests {

    private static final String MACHINE_NAME = "f001.node.example.com";

    @Autowired
    private ApiMachineManagementService apiMachineManagementService;

    public ApiMachineManagementServiceImplTest() {
        super(true);
    }

    @Test
    public void testAddSystemStats_FAIL() {

        String userId = FakeDataServiceImpl.API_USER_MACHINE_ID_F002;

        setApiAuth(userId);

        List<SystemStats> systemStats = Arrays.asList(new SystemStats());
        FormResult result = apiMachineManagementService.addSystemStats(userId, MACHINE_NAME, systemStats);

        FormResult expected = new FormResult();
        expected.getGlobalErrors().add("You are not allowed");

        AssertTools.assertJsonComparisonWithoutNulls(expected, result);
    }

    @Test
    public void testAddSystemStats_OK() {

        String userId = FakeDataServiceImpl.API_USER_MACHINE_ID_F001;

        setApiAuth(userId);

        List<SystemStats> systemStats = Arrays.asList(new SystemStats());
        FormResult result = apiMachineManagementService.addSystemStats(userId, MACHINE_NAME, systemStats);

        FormResult expected = new FormResult();
        AssertTools.assertJsonComparisonWithoutNulls(expected, result);
    }

    private void testGetMachineSetup_FAIL(String userId, String expectedError) {
        testGetMachineSetup_FAIL(MACHINE_NAME, userId, expectedError);
    }

    private void testGetMachineSetup_FAIL(String machineName, String userId, String expectedError) {

        setApiAuth(userId);

        ResponseMachineSetup result = apiMachineManagementService.getMachineSetup(userId, machineName);

        if (result.getError() != null) {
            result.getError().setTimestamp(null);
            result.getError().setUniqueId(null);
        }

        ResponseMachineSetup expected = new ResponseMachineSetup();
        expected.setError(new ApiError((String) null, (String) null, expectedError));

        AssertTools.assertJsonComparisonWithoutNulls(expected, result);
    }

    @Test
    public void testGetMachineSetup_FAIL_ApiMachineUser_another_one() {
        testGetMachineSetup_FAIL(FakeDataServiceImpl.API_USER_MACHINE_ID_F002, "You are not allowed");
    }

    @Test
    public void testGetMachineSetup_FAIL_ApiUser_Not_Admin() {
        testGetMachineSetup_FAIL(FakeDataServiceImpl.API_USER_ID_USER_ALPHA, "You are not allowed");
    }

    @Test
    public void testGetMachineSetup_FAIL_MachineNotExists() {
        testGetMachineSetup_FAIL("not.node.example.com", FakeDataServiceImpl.USER_ID_ADMIN, "The machine does not exist");
    }

    @Test
    public void testGetMachineSetup_FAIL_Noone() {
        testGetMachineSetup_FAIL(null, "You are not allowed");
    }

    @Test
    public void testGetMachineSetup_FAIL_User_Not_Admin() {
        testGetMachineSetup_FAIL(FakeDataServiceImpl.USER_ID_BETA, "You are not allowed");
    }

    private void testGetMachineSetup_OK(String userId) {

        setApiAuth(userId);

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

    private void testGetMachineSetup_WithIpChange_FAIL(String userId, String expectedError) {
        testGetMachineSetup_WithIpChange_FAIL(MACHINE_NAME, userId, expectedError);
    }

    private void testGetMachineSetup_WithIpChange_FAIL(String machineName, String userId, String expectedError) {

        setApiAuth(userId);

        ResponseMachineSetup result = apiMachineManagementService.getMachineSetup(userId, machineName, "11.0.0.10");

        if (result.getError() != null) {
            result.getError().setTimestamp(null);
            result.getError().setUniqueId(null);
        }

        ResponseMachineSetup expected = new ResponseMachineSetup();
        expected.setError(new ApiError((String) null, (String) null, expectedError));

        AssertTools.assertJsonComparisonWithoutNulls(expected, result);
    }

    @Test
    public void testGetMachineSetup_WithIpChange_FAIL_ApiMachineUser_another_one() {
        testGetMachineSetup_WithIpChange_FAIL(FakeDataServiceImpl.API_USER_MACHINE_ID_F002, "You are not allowed");
    }

    private void testGetMachineSetup_WithIpChange_OK(String userId) {

        setApiAuth(userId);

        ResponseMachineSetup result = apiMachineManagementService.getMachineSetup(userId, MACHINE_NAME, "11.0.0.10");

        if (result.getItem() != null) {
            result.setItem(new MachineSetup());
        }

        ResponseMachineSetup expected = new ResponseMachineSetup();
        expected.setItem(new MachineSetup());

        AssertTools.assertJsonComparisonWithoutNulls(expected, result);
    }

    @Test
    public void testGetMachineSetup_WithIpChange_OK_ApiMachineUser_right_one() {
        testGetMachineSetup_WithIpChange_OK(FakeDataServiceImpl.API_USER_MACHINE_ID_F001);
    }

    @Test
    public void testIsIp4AndPublic() {
        Assert.assertTrue(ApiMachineManagementServiceImpl.isIp4AndPublic("138.197.140.49"));
        Assert.assertTrue(ApiMachineManagementServiceImpl.isIp4AndPublic("138.197.169.2"));
        Assert.assertTrue(ApiMachineManagementServiceImpl.isIp4AndPublic("142.93.155.229"));
        Assert.assertTrue(ApiMachineManagementServiceImpl.isIp4AndPublic("165.22.228.192"));
        Assert.assertTrue(ApiMachineManagementServiceImpl.isIp4AndPublic("167.99.185.12"));
        Assert.assertTrue(ApiMachineManagementServiceImpl.isIp4AndPublic("178.128.232.111"));

        // Not IPv4
        Assert.assertFalse(ApiMachineManagementServiceImpl.isIp4AndPublic("138.197.169.2.3"));

        // 127.0.0.0 to 127.255.255.255
        Assert.assertFalse(ApiMachineManagementServiceImpl.isIp4AndPublic("127.0.0.1"));
        Assert.assertFalse(ApiMachineManagementServiceImpl.isIp4AndPublic("127.3.0.1"));

        // 10.0.0.0 to 10.255.255.255
        Assert.assertFalse(ApiMachineManagementServiceImpl.isIp4AndPublic("10.0.0.1"));
        Assert.assertFalse(ApiMachineManagementServiceImpl.isIp4AndPublic("10.3.0.1"));
        Assert.assertTrue(ApiMachineManagementServiceImpl.isIp4AndPublic("11.3.0.1"));

        // 169.254.0.0 to 169.254.255.255
        Assert.assertTrue(ApiMachineManagementServiceImpl.isIp4AndPublic("169.253.0.1"));
        Assert.assertFalse(ApiMachineManagementServiceImpl.isIp4AndPublic("169.254.0.1"));
        Assert.assertFalse(ApiMachineManagementServiceImpl.isIp4AndPublic("169.254.10.10"));

        // 172.16.0.0 to 172.31.255.255
        Assert.assertTrue(ApiMachineManagementServiceImpl.isIp4AndPublic("172.15.0.7"));
        Assert.assertFalse(ApiMachineManagementServiceImpl.isIp4AndPublic("172.17.0.7"));
        Assert.assertTrue(ApiMachineManagementServiceImpl.isIp4AndPublic("172.32.0.7"));

        // 192.168.0.0 to 192.168.255.255
        Assert.assertTrue(ApiMachineManagementServiceImpl.isIp4AndPublic("192.167.0.2"));
        Assert.assertFalse(ApiMachineManagementServiceImpl.isIp4AndPublic("192.168.0.2"));
        Assert.assertFalse(ApiMachineManagementServiceImpl.isIp4AndPublic("192.168.200.2"));
        Assert.assertTrue(ApiMachineManagementServiceImpl.isIp4AndPublic("192.169.200.2"));
    }

}
