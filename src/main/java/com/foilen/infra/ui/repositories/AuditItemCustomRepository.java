/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.repositories;

import org.springframework.data.domain.Page;

import com.foilen.infra.api.request.RequestAuditItem;
import com.foilen.infra.plugin.v1.core.eventhandler.changes.AuditUserType;
import com.foilen.infra.ui.repositories.documents.AuditItem;

public interface AuditItemCustomRepository {

    Page<AuditItem> findAll(RequestAuditItem request);

    void updateImpersonatorByTxId(String txId, String userId, AuditUserType userType);

}
