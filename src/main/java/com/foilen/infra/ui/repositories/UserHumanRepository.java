/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.foilen.infra.ui.repositories.documents.UserHuman;

public interface UserHumanRepository extends MongoRepository<UserHuman, String> {

    Page<UserHuman> findAllByUserIdLikeOrEmailLike(String userId, String email, Pageable pageable);

    Optional<UserHuman> findByEmail(String email);

}
