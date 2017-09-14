/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
 package com.foilen.infra.ui.db.dao;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.foilen.infra.ui.db.domain.user.ApiUser;

@Service
public interface ApiUserDao extends JpaRepository<ApiUser, Long> {

    void deleteAllByExpireOnLessThanEqual(Date expiredOnBefore);

    ApiUser findByUserId(String userId);

    ApiUser findByUserIdAndExpireOnAfter(String userId, Date expiredOnAfter);

}
