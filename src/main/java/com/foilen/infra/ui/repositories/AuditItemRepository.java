/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.foilen.infra.ui.repositories.documents.AuditItem;

public interface AuditItemRepository extends MongoRepository<AuditItem, String>, AuditItemCustomRepository {

    Page<AuditItem> findAllByTxId(String txId, Pageable page);

    AuditItem findFirstByTypeOrderByIdDesc(String type);

}
