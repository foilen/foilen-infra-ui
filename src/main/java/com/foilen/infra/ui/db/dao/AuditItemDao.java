/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.db.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.foilen.infra.ui.db.domain.audit.AuditItem;

@Service
public interface AuditItemDao extends JpaRepository<AuditItem, Long> {

    Page<AuditItem> findAllByTxId(String txId, Pageable page);

    AuditItem findFirstByTypeOrderByIdDesc(String type);

}
