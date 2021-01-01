/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.repositories;

import java.util.Date;

import com.foilen.infra.ui.repositories.documents.UserApi;

public interface UserApiCustomRepository {

    UserApi findByUserIdAndActive(String userId, Date expireAfter);

}
