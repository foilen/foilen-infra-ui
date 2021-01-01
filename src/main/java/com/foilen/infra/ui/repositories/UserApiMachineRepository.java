/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.repositories;

import java.util.Date;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.foilen.infra.ui.repositories.documents.UserApiMachine;

public interface UserApiMachineRepository extends MongoRepository<UserApiMachine, String> {

    void deleteAllByExpireOnLessThanEqual(Date now);

    UserApiMachine findFirstByMachineNameAndExpireOnAfterOrderByExpireOnDesc(String machineName, Date expireAfter);

}
