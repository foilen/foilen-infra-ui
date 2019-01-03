/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.Calendar;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.foilen.infra.ui.db.dao.ApiMachineUserDao;
import com.foilen.infra.ui.db.domain.user.ApiMachineUser;
import com.foilen.infra.ui.test.AbstractSpringTests;
import com.foilen.smalltools.tools.DateTools;

public class ApiUserServiceImplTest extends AbstractSpringTests {

    @Autowired
    private ApiUserService apiUserService;
    @Autowired
    private ApiMachineUserDao apiMachineUserDao;

    public ApiUserServiceImplTest() {
        super(true);
    }

    private String getIdAndKey(ApiMachineUser apiMachineUser) {
        return apiMachineUser.getUserId() + apiMachineUser.getUserHashedKey();
    }

    @Test
    public void testMachineApi() {

        apiMachineUserDao.deleteAll();

        String m1Name = "f001.node.example.com";
        String m2Name = "f002.node.example.com";

        Assert.assertEquals(0, apiMachineUserDao.count());

        Date now = DateTools.addDate(Calendar.MINUTE, 1);

        // Adding both machines and always getting the right id and key
        String m1IdAndKey = getIdAndKey(apiUserService.getOrCreateForMachine(m1Name, now));
        Assert.assertEquals(1, apiMachineUserDao.count());
        Assert.assertEquals(m1IdAndKey, getIdAndKey(apiUserService.getOrCreateForMachine(m1Name, now)));
        Assert.assertEquals(1, apiMachineUserDao.count());
        String m2IdAndKey = getIdAndKey(apiUserService.getOrCreateForMachine(m2Name, now));
        Assert.assertEquals(2, apiMachineUserDao.count());
        Assert.assertEquals(m2IdAndKey, getIdAndKey(apiUserService.getOrCreateForMachine(m2Name, now)));
        Assert.assertEquals(2, apiMachineUserDao.count());
        Assert.assertEquals(m1IdAndKey, getIdAndKey(apiUserService.getOrCreateForMachine(m1Name, now)));
        Assert.assertEquals(2, apiMachineUserDao.count());

        apiUserService.deleteExpired(now);
        Assert.assertEquals(2, apiMachineUserDao.count());

        // Advance a bit in time
        now = DateTools.addDate(now, Calendar.DAY_OF_YEAR, 15); // Day 15
        apiUserService.deleteExpired(now);
        Assert.assertEquals(2, apiMachineUserDao.count());
        Assert.assertEquals(m1IdAndKey, getIdAndKey(apiUserService.getOrCreateForMachine(m1Name, now)));
        Assert.assertEquals(m2IdAndKey, getIdAndKey(apiUserService.getOrCreateForMachine(m2Name, now)));
        Assert.assertEquals(2, apiMachineUserDao.count());

        // Near expiration
        now = DateTools.addDate(now, Calendar.DAY_OF_YEAR, 5); // Day 20 ; need refresh
        apiUserService.deleteExpired(now);
        Assert.assertEquals(2, apiMachineUserDao.count());
        Assert.assertNotEquals(m1IdAndKey, getIdAndKey(apiUserService.getOrCreateForMachine(m1Name, now)));
        Assert.assertEquals(3, apiMachineUserDao.count());
        Assert.assertNotEquals(m2IdAndKey, getIdAndKey(apiUserService.getOrCreateForMachine(m2Name, now)));
        Assert.assertEquals(4, apiMachineUserDao.count());

        m1IdAndKey = getIdAndKey(apiUserService.getOrCreateForMachine(m1Name, now));
        m2IdAndKey = getIdAndKey(apiUserService.getOrCreateForMachine(m2Name, now));
        Assert.assertEquals(4, apiMachineUserDao.count());

        apiUserService.deleteExpired(now);
        Assert.assertEquals(4, apiMachineUserDao.count());

        // Expiration
        now = DateTools.addDate(now, Calendar.DAY_OF_YEAR, 10); // Day 30 / Day 5 ; expired
        apiUserService.deleteExpired(now);
        Assert.assertEquals(2, apiMachineUserDao.count());
        Assert.assertEquals(m1IdAndKey, getIdAndKey(apiUserService.getOrCreateForMachine(m1Name, now)));
        Assert.assertEquals(m2IdAndKey, getIdAndKey(apiUserService.getOrCreateForMachine(m2Name, now)));
        Assert.assertEquals(2, apiMachineUserDao.count());

    }

}
