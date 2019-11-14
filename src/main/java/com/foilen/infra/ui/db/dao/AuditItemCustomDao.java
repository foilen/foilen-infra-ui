/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.db.dao;

import org.springframework.data.domain.Page;

import com.foilen.infra.apitmp.request.RequestAuditItem;
import com.foilen.infra.ui.db.domain.audit.AuditItem;

public interface AuditItemCustomDao {

    Page<AuditItem> findAll(RequestAuditItem request);

}
