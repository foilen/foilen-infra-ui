/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.repositories;

import java.util.Date;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.foilen.infra.ui.repositories.documents.UserApi;

public interface UserApiRepository extends MongoRepository<UserApi, String>, UserApiCustomRepository {

    void deleteAllByExpireOnLessThanEqual(Date now);

}
