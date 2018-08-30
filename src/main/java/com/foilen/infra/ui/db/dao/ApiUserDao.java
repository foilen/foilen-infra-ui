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
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import com.foilen.infra.ui.db.domain.user.ApiUser;

@Service
public interface ApiUserDao extends JpaRepository<ApiUser, Long> {

    void deleteAllByExpireOnLessThanEqual(Date expiredOnBefore);

    void deleteByUserId(String userId);

    ApiUser findByUserId(String userId);

    @Query("SELECT au FROM ApiUser au WHERE au.userId = :userId AND (au.expireOn IS NULL OR au.expireOn > :expiredOnAfter)")
    ApiUser findByUserIdAndActive(@Param("userId") String userId, @Param("expiredOnAfter") Date expiredOnAfter);

}
