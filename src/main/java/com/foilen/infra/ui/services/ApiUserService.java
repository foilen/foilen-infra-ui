/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.Date;
import java.util.List;

import com.foilen.infra.ui.db.domain.user.ApiMachineUser;
import com.foilen.infra.ui.db.domain.user.ApiUser;
import com.foilen.smalltools.tuple.Tuple2;

public interface ApiUserService {

    /**
     * Create a new API User.
     *
     * @return the user id and password
     */
    Tuple2<String, String> createAdminUser();

    void deleteExpired();

    void deleteExpired(Date now);

    void deleteUser(String userId);

    List<ApiUser> findAll();

    ApiUser findByUserIdAndActive(String userId);

    ApiMachineUser getOrCreateForMachine(String machineName);

    ApiMachineUser getOrCreateForMachine(String machineName, Date now);

}
