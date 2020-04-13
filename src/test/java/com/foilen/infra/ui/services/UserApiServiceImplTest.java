/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.Calendar;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.foilen.infra.ui.repositories.UserApiMachineRepository;
import com.foilen.infra.ui.repositories.documents.UserApiMachine;
import com.foilen.infra.ui.test.AbstractSpringTests;
import com.foilen.smalltools.tools.DateTools;

public class UserApiServiceImplTest extends AbstractSpringTests {

    @Autowired
    private UserApiService userApiService;
    @Autowired
    private UserApiMachineRepository userApiMachineRepository;

    public UserApiServiceImplTest() {
        super(true);
    }

    private String getIdAndKey(UserApiMachine apiMachineUser) {
        return apiMachineUser.getUserId() + apiMachineUser.getUserHashedKey();
    }

    @Test
    public void testMachineApi() {

        userApiMachineRepository.deleteAll();

        String m1Name = "f001.node.example.com";
        String m2Name = "f002.node.example.com";

        Assert.assertEquals(0, userApiMachineRepository.count());

        Date now = DateTools.addDate(Calendar.MINUTE, 1);

        // Adding both machines and always getting the right id and key
        String m1IdAndKey = getIdAndKey(userApiService.getOrCreateForMachine(m1Name, now));
        Assert.assertEquals(1, userApiMachineRepository.count());
        Assert.assertEquals(m1IdAndKey, getIdAndKey(userApiService.getOrCreateForMachine(m1Name, now)));
        Assert.assertEquals(1, userApiMachineRepository.count());
        String m2IdAndKey = getIdAndKey(userApiService.getOrCreateForMachine(m2Name, now));
        Assert.assertEquals(2, userApiMachineRepository.count());
        Assert.assertEquals(m2IdAndKey, getIdAndKey(userApiService.getOrCreateForMachine(m2Name, now)));
        Assert.assertEquals(2, userApiMachineRepository.count());
        Assert.assertEquals(m1IdAndKey, getIdAndKey(userApiService.getOrCreateForMachine(m1Name, now)));
        Assert.assertEquals(2, userApiMachineRepository.count());

        userApiService.deleteExpired(now);
        Assert.assertEquals(2, userApiMachineRepository.count());

        // Advance a bit in time
        now = DateTools.addDate(now, Calendar.DAY_OF_YEAR, 15); // Day 15
        userApiService.deleteExpired(now);
        Assert.assertEquals(2, userApiMachineRepository.count());
        Assert.assertEquals(m1IdAndKey, getIdAndKey(userApiService.getOrCreateForMachine(m1Name, now)));
        Assert.assertEquals(m2IdAndKey, getIdAndKey(userApiService.getOrCreateForMachine(m2Name, now)));
        Assert.assertEquals(2, userApiMachineRepository.count());

        // Near expiration
        now = DateTools.addDate(now, Calendar.DAY_OF_YEAR, 5); // Day 20 ; need refresh
        userApiService.deleteExpired(now);
        Assert.assertEquals(2, userApiMachineRepository.count());
        Assert.assertNotEquals(m1IdAndKey, getIdAndKey(userApiService.getOrCreateForMachine(m1Name, now)));
        Assert.assertEquals(3, userApiMachineRepository.count());
        Assert.assertNotEquals(m2IdAndKey, getIdAndKey(userApiService.getOrCreateForMachine(m2Name, now)));
        Assert.assertEquals(4, userApiMachineRepository.count());

        m1IdAndKey = getIdAndKey(userApiService.getOrCreateForMachine(m1Name, now));
        m2IdAndKey = getIdAndKey(userApiService.getOrCreateForMachine(m2Name, now));
        Assert.assertEquals(4, userApiMachineRepository.count());

        userApiService.deleteExpired(now);
        Assert.assertEquals(4, userApiMachineRepository.count());

        // Expiration
        now = DateTools.addDate(now, Calendar.DAY_OF_YEAR, 10); // Day 30 / Day 5 ; expired
        userApiService.deleteExpired(now);
        Assert.assertEquals(2, userApiMachineRepository.count());
        Assert.assertEquals(m1IdAndKey, getIdAndKey(userApiService.getOrCreateForMachine(m1Name, now)));
        Assert.assertEquals(m2IdAndKey, getIdAndKey(userApiService.getOrCreateForMachine(m2Name, now)));
        Assert.assertEquals(2, userApiMachineRepository.count());

    }

}
