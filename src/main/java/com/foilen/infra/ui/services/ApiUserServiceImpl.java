/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

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

import com.foilen.infra.ui.db.dao.ApiMachineUserDao;
import com.foilen.infra.ui.db.dao.ApiUserDao;
import com.foilen.infra.ui.db.domain.user.ApiMachineUser;
import com.foilen.infra.ui.db.domain.user.ApiUser;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.DateTools;
import com.foilen.smalltools.tools.SecureRandomTools;
import com.foilen.smalltools.tuple.Tuple2;

@Service
@Transactional
public class ApiUserServiceImpl extends AbstractBasics implements ApiUserService {

    private static final int MACHINE_EXPIRE_DAYS = 30;
    private static final int MACHINE_REFRESH_BEFORE_EXPIRE_DAYS = MACHINE_EXPIRE_DAYS / 3; // 1/3 of the expiration time

    @Autowired
    private ApiUserDao apiUserDao;
    @Autowired
    private ApiMachineUserDao apiMachineUserDao;

    @Override
    public void deleteExpired() {
        deleteExpired(new Date());
    }

    @Override
    public void deleteExpired(Date now) {
        apiUserDao.deleteAllByExpireOnLessThanEqual(now);
    }

    @Override
    public ApiUser findByUserIdAndActive(String userId) {
        return apiUserDao.findByUserIdAndActive(userId, new Date());
    }

    protected Tuple2<String, String> genIdAndKey() {
        return new Tuple2<>(SecureRandomTools.randomHexString(25), SecureRandomTools.randomHexString(25));
    }

    @Override
    public ApiMachineUser getOrCreateForMachine(String machineName) {
        return getOrCreateForMachine(machineName, new Date());
    }

    @Override
    public ApiMachineUser getOrCreateForMachine(String machineName, Date now) {
        Date expireAfter = DateTools.addDate(now, Calendar.DAY_OF_YEAR, MACHINE_REFRESH_BEFORE_EXPIRE_DAYS);

        ApiMachineUser apiMachineUser = apiMachineUserDao.findFirstByMachineNameAndExpireOnAfterOrderByExpireOnDesc(machineName, expireAfter);
        boolean createNew = false;
        createNew |= apiMachineUser == null;

        if (createNew) {
            Tuple2<String, String> generated = genIdAndKey();
            Date expireOn = DateTools.addDate(now, Calendar.DAY_OF_YEAR, MACHINE_EXPIRE_DAYS);
            apiMachineUser = apiMachineUserDao.save(new ApiMachineUser(generated.getA(), generated.getB(), BCrypt.hashpw(generated.getB(), BCrypt.gensalt(13)), machineName, expireOn));
        }

        return apiMachineUser;

    }

}
