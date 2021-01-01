/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import org.springframework.data.domain.Page;

import com.foilen.infra.plugin.v1.core.eventhandler.changes.AuditUserType;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.infra.ui.repositories.documents.AuditItem;

public interface AuditingService {

    void documentAdd(String userName, Object document);

    void documentDelete(String userName, Object document);

    void documentEdit(String userName, Object documentFrom, Object documentTo);

    Page<AuditItem> findAllByTxId(String txId, int pageId, int itemsPerPage);

    void linkAdd(String txId, boolean explicitChange, //
            AuditUserType userType, String userName, //
            IPResource fromResource, String linkType, IPResource toResource //
    );

    void linkDelete(String txId, boolean explicitChange, //
            AuditUserType userType, String userName, //
            IPResource fromResource, String linkType, IPResource toResource //
    );

    void resourceAdd(String txId, boolean explicitChange, //
            AuditUserType userType, String userName, //
            IPResource resource //
    );

    void resourceDelete(String txId, boolean explicitChange, //
            AuditUserType userType, String userName, //
            IPResource resource //
    );

    void resourceUpdate(String txId, boolean explicitChange, //
            AuditUserType userType, String userName, //
            IPResource beforeResource, IPResource afterResource //
    );

    void tagAdd(String txId, boolean explicitChange, //
            AuditUserType userType, String userName, //
            IPResource resource, String tagName //
    );

    void tagDelete(String txId, boolean explicitChange, //
            AuditUserType userType, String userName, //
            IPResource resource, String tagName //
    );

}
