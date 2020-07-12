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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foilen.infra.ui.repositories.UserApiMachineRepository;
import com.foilen.infra.ui.repositories.UserApiRepository;
import com.foilen.infra.ui.repositories.documents.UserApi;
import com.foilen.infra.ui.repositories.documents.UserApiMachine;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.DateTools;
import com.foilen.smalltools.tools.SecureRandomTools;
import com.foilen.smalltools.tuple.Tuple2;

@Service
@Transactional
public class UserApiServiceImpl extends AbstractBasics implements UserApiService {

    private static final int MACHINE_EXPIRE_DAYS = 30;
    private static final int MACHINE_REFRESH_BEFORE_EXPIRE_DAYS = MACHINE_EXPIRE_DAYS / 3; // 1/3 of the expiration time

    @Autowired
    private UserApiRepository userApiRepository;
    @Autowired
    private UserApiMachineRepository userApiMachineRepository;

    @Override
    public void deleteExpired() {
        deleteExpired(new Date());
    }

    @Override
    public void deleteExpired(Date now) {
        userApiRepository.deleteAllByExpireOnLessThanEqual(now);
        userApiMachineRepository.deleteAllByExpireOnLessThanEqual(now);
    }

    @Override
    public void deleteUser(String userId) {
        userApiRepository.deleteById(userId);
        userApiMachineRepository.deleteById(userId);
    }

    @Override
    public UserApi findByUserIdAndActive(String userId) {
        return userApiRepository.findByUserIdAndActive(userId, new Date());
    }

    protected Tuple2<String, String> genIdAndKey() {
        return new Tuple2<>(SecureRandomTools.randomHexString(25), SecureRandomTools.randomHexString(25));
    }

    @Override
    public UserApiMachine getOrCreateForMachine(String machineName) {
        return getOrCreateForMachine(machineName, new Date());
    }

    @Override
    public UserApiMachine getOrCreateForMachine(String machineName, Date now) {
        Date expireAfter = DateTools.addDate(now, Calendar.DAY_OF_YEAR, MACHINE_REFRESH_BEFORE_EXPIRE_DAYS);

        UserApiMachine apiMachineUser = userApiMachineRepository.findFirstByMachineNameAndExpireOnAfterOrderByExpireOnDesc(machineName, expireAfter);
        boolean createNew = false;
        createNew |= apiMachineUser == null;

        if (createNew) {
            Tuple2<String, String> generated = genIdAndKey();
            Date expireOn = DateTools.addDate(now, Calendar.DAY_OF_YEAR, MACHINE_EXPIRE_DAYS);
            apiMachineUser = userApiMachineRepository.save(new UserApiMachine(generated.getA(), generated.getB(), BCrypt.hashpw(generated.getB(), BCrypt.gensalt(13)), machineName, expireOn));
        }

        return apiMachineUser;

    }

}
