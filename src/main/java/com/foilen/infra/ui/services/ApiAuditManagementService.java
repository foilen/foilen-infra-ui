/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import com.foilen.infra.apitmp.model.AuditItemWithPagination;
import com.foilen.infra.apitmp.request.RequestAuditItem;

public interface ApiAuditManagementService {

    AuditItemWithPagination auditFindAll(String userId, RequestAuditItem request);

}
