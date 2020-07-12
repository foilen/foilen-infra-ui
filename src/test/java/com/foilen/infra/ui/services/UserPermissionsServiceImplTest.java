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

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.foilen.infra.ui.test.AbstractSpringTests;
import com.foilen.infra.ui.test.mock.FakeDataServiceImpl;
import com.foilen.smalltools.test.asserts.AssertTools;

public class UserPermissionsServiceImplTest extends AbstractSpringTests {

    @Autowired
    private UserPermissionsService userPermissionsService;

    public UserPermissionsServiceImplTest() {
        super(true);
    }

    @Test
    public void testFindOwnersThatUserCanCreateAs_Admin() {
        List<String> expectedOwners = Arrays.asList("", "alpha", "beta", "infra", "shared");
        AssertTools.assertJsonComparison(expectedOwners, userPermissionsService.findOwnersThatUserCanCreateAs(FakeDataServiceImpl.USER_ID_ADMIN));
    }

    @Test
    public void testFindOwnersThatUserCanCreateAs_User() {
        List<String> expectedOwners = Arrays.asList("alpha");
        AssertTools.assertJsonComparison(expectedOwners, userPermissionsService.findOwnersThatUserCanCreateAs(FakeDataServiceImpl.USER_ID_ALPHA));
    }

}
