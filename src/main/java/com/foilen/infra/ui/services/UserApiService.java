/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.Date;

import com.foilen.infra.ui.repositories.documents.UserApi;
import com.foilen.infra.ui.repositories.documents.UserApiMachine;

public interface UserApiService {

    void deleteExpired();

    void deleteExpired(Date now);

    void deleteUser(String userId);

    UserApi findByUserIdAndActive(String userId);

    UserApiMachine getOrCreateForMachine(String machineName);

    UserApiMachine getOrCreateForMachine(String machineName, Date now);

}
