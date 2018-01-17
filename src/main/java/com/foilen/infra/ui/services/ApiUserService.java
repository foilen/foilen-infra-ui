/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.Date;

import com.foilen.infra.ui.db.domain.user.ApiMachineUser;
import com.foilen.infra.ui.db.domain.user.ApiUser;

public interface ApiUserService {

    void deleteExpired();

    void deleteExpired(Date now);

    ApiUser findByUserIdAndActive(String userId);

    ApiMachineUser getOrCreateForMachine(String machineName);

    ApiMachineUser getOrCreateForMachine(String machineName, Date now);

}
