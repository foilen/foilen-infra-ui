/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.db.dao;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.foilen.infra.ui.db.domain.user.ApiMachineUser;

@Service
public interface ApiMachineUserDao extends JpaRepository<ApiMachineUser, Long> {

    ApiMachineUser findByUserId(String userId);

    ApiMachineUser findFirstByMachineNameAndExpireOnAfterOrderByExpireOnDesc(String machineName, Date expireAfter);

}
