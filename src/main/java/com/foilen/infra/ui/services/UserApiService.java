/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.Date;
import java.util.List;

import com.foilen.infra.ui.repositories.documents.UserApi;
import com.foilen.infra.ui.repositories.documents.UserApiMachine;
import com.foilen.smalltools.tuple.Tuple2;

public interface UserApiService {

    /**
     * Create a new API User.
     *
     * @return the user id and password
     */
    Tuple2<String, String> createAdminUser();

    void deleteExpired();

    void deleteExpired(Date now);

    void deleteUser(String userId);

    List<UserApi> findAll();

    UserApi findByUserIdAndActive(String userId);

    UserApiMachine getOrCreateForMachine(String machineName);

    UserApiMachine getOrCreateForMachine(String machineName, Date now);

}
