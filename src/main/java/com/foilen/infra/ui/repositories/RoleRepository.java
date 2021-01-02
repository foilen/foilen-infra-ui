/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.repositories;

import java.util.Collection;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.foilen.infra.ui.repositories.documents.Role;

public interface RoleRepository extends MongoRepository<Role, String> {

    Stream<Role> findAllByNameIn(Collection<String> roles);

    @Query(fields = "{name : 1}")
    Page<Role> findAllNamesBy(Pageable pageable);

    @Query(fields = "{name : 1}")
    Page<Role> findAllNanesByNameLike(String search, Pageable pageable);

}
